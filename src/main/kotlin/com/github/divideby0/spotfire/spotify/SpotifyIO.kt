package com.github.divideby0.spotfire.spotify

import com.amazonaws.services.dynamodbv2.model.TableDescription
import com.github.divideby0.spotfire.domain.SpotfirePlaylist
import com.github.divideby0.spotfire.proto.SpotifyProtos
import com.wrapper.spotify.IHttpManager
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.exceptions.detailed.NotFoundException
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysis
import com.wrapper.spotify.model_objects.specification.*
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.util.concurrent.ExecutionException

class SpotifyIO(
    val clientId: String,
    val clientSecret: String,
    val audioFeaturesPerRequest: Int = 100,
    val albumsPerRequest: Int = 20,
    val artistsPerRequest: Int = 50,
    val tracksPerPlaylistAddition: Int = 20,
    val rateLimit: Double = 10.0,
    private val httpManager: IHttpManager = SpotfireHttpManager(
        requestsPerSecond = rateLimit,
        totalConnections = rateLimit.toInt() * 3
    )
) {
    val log = LoggerFactory.getLogger(this.javaClass)

    val playlistUriPattern = Regex(".*user[:/]([^:/]+)[:/]playlist[:/]([^?]+).*")

    fun getApiFromRefreshToken(refreshToken: String): SpotifyApi {
        val authApi = SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRefreshToken(refreshToken)
            .build()

        val authResp = authApi
            .authorizationCodeRefresh()
            .build()
            .execute()

        return SpotifyApi.Builder()
            .setAccessToken(authResp.accessToken)
            .setRefreshToken(authResp.refreshToken)
            .setHttpManager(httpManager)
            .build()
    }

    fun getUserProto(api: SpotifyApi, userId: String): SpotifyProtos.User {
        log.info("Getting user info for $userId")
        return SpotifyProtoUtils.toUserProto(api.getUsersProfile(userId).build().execute())
    }

    fun getPlaylistProto(api: SpotifyApi, userId: String, playlistId: String): SpotifyProtos.Playlist {
        log.info("Getting playlist $playlistId from user $userId")
        val playlist = api.getPlaylist(userId, playlistId).build().execute()

        val b = SpotifyProtos.Playlist.newBuilder()
        b.id = playlist.id
        b.uri = playlist.uri
        b.name = playlist.name
        b.snapshotId = playlist.snapshotId
        b.owner = getUserProto(api, playlist.owner.id)

        val playlistTracks = getAllTracksInPlaylist(api, playlist).map { it.track }
        val tracksWithDetails = getTracksAsProto(api, playlistTracks)
        b.addAllTracks(tracksWithDetails)

        val artistIds = tracksWithDetails.flatMap { it.artistIdsList }.distinct()
        b.addAllArtists(getArtistsAsProto(api, artistIds))

        val albumIds = tracksWithDetails.map { it.albumId }.distinct()
        b.addAllAlbums(getAlbumAsProto(api, albumIds))

        return b.build()
    }

    fun getPlaylistProto(refreshToken: String, userId: String, playlistId: String): SpotifyProtos.Playlist {
        val api = getApiFromRefreshToken(refreshToken)
        return getPlaylistProto(api, userId, playlistId)
    }

    fun getPlaylistProto(refreshToken: String, playlistUri: String): SpotifyProtos.Playlist {
        val api = getApiFromRefreshToken(refreshToken)
        val match = playlistUriPattern.matchEntire(playlistUri)
        if(match != null) {
            val (userId, playlistId) = match.destructured
            return getPlaylistProto(api, userId, playlistId)
        } else {
            throw IllegalArgumentException("Could not parse playlist uri: $playlistUri")
        }
    }

    private fun getArtistsAsProto(api: SpotifyApi, artistIds: List<String>): List<SpotifyProtos.Artist> {
        return artistIds.chunked(artistsPerRequest).map { chunk ->
            val idStr = chunk.joinToString(",")
            log.info("Getting artist details for ${idStr}")
            api.getSeveralArtists(idStr).build().executeAsync<Array<out Artist>>()
        }.flatMap { it.get().map(SpotifyProtoUtils::toArtistProto) }
    }

    private fun getAlbumAsProto(api: SpotifyApi, albumIds: List<String>): List<SpotifyProtos.Album> {
        return albumIds.chunked(albumsPerRequest).map { chunk ->
            val idStr = chunk.joinToString(",")
            log.info("Getting album details for $idStr")
            api.getSeveralAlbums(idStr).build().executeAsync<Array<out Album>>()
        }.flatMap { it.get().map(SpotifyProtoUtils::toAlbumProto) }
    }

    

    private fun getTracksAsProto(api: SpotifyApi, tracks: List<Track>): Iterable<SpotifyProtos.Track> {
        return tracks
            .chunked(audioFeaturesPerRequest)
            .flatMap { chunk ->
                val trackIds = chunk.map { it.id }.joinToString(",")
                log.info("Getting audio features for tracks $trackIds")
                val features = api.getAudioFeaturesForSeveralTracks(trackIds).build().execute()
                chunk.zip(features)
            }
            .map { (track, features) ->
                val analysisFuture = api.getAudioAnalysisForTrack(track.id).build().executeAsync<AudioAnalysis>()
                Triple(track, features, analysisFuture)
            }
            .map { (track, features, analysisFuture) ->
                val trackName = "${track.artists.joinToString(" + ") { it.name }} - ${track.name}"
                try {
                    val analysis = analysisFuture.get()
                    log.info("Fetched audio analysis for ${track.id}: $trackName")
                    SpotifyProtoUtils.toTrackProto(track, features, analysis)
                } catch(e: ExecutionException) {
                    val cause = e.cause
                    if(cause is NotFoundException) {
                        log.warn("Could not find audio analysis for ${track.id}: $trackName")
                    } else if(cause != null) {
                        throw cause
                    }
                    SpotifyProtoUtils.toTrackProto(track, features, null)
                }
            }
    }

    fun getAllTracksInPlaylist(api: SpotifyApi, playlist: Playlist): List<PlaylistTrack> {
        val paging = playlist.tracks
        val tracks = mutableListOf(*paging.items)
        if(paging.total > paging.items.size) {
            val limit = paging.items.size
            for (offset in paging.items.size..paging.total-1 step limit) {
                log.info("Getting additional tracks for playlist ${playlist.uri}, offset: $offset, limit: $limit")
                val page = api.getPlaylistsTracks(playlist.owner.id, playlist.id)
                    .offset(offset)
                    .limit(limit)
                    .build()
                    .execute()
                tracks.addAll(page.items)
            }
        }
        return tracks.toList()
    }

    fun savePlaylist(api: SpotifyApi, solution: SpotfirePlaylist, playlistName: String, description: String, public: Boolean = true): Playlist {
        val user = api.currentUsersProfile.build().execute()
        val trackUris = solution.assignments.map { it.track?.spotifyUri }
        log.info("Creating playlist '$playlistName' for user ${user.id} with ${trackUris.size} tracks")

        val playlist = api
            .createPlaylist(user.id, playlistName)
            .description(description)
            .public_(public)
            .build()
            .execute()

        trackUris.chunked(tracksPerPlaylistAddition).forEach { chunk ->
            log.debug("Adding ${chunk.size} tracks to playlist $playlistName")
            api.addTracksToPlaylist(user.id, playlist.id, chunk.toTypedArray()).build().execute()
        }

        return playlist
    }
}