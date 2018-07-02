package com.github.divideby0.spotfire.utils

import com.neovisionaries.i18n.CountryCode
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.specification.*
import org.slf4j.LoggerFactory

class SpotifyClient(
	val spotifyApiBuilder: SpotifyApi.Builder = SpotifyApi.Builder(),
	val accessToken: String,
	val spotifyApi: SpotifyApi = spotifyApiBuilder.setAccessToken(accessToken).build()
) {
	val log = LoggerFactory.getLogger(this.javaClass)

  val playlistUriPattern = Regex("spotify:user:([^:]+):playlist:(\\w+)")

	fun getPlaylistUrl(playlist: Playlist) = "https://open.spotify.com/user/${playlist.owner.id}/playlist/${playlist.id}"

  fun getPlaylist(spotifyUri: String): Playlist? {
    // spotify:user:thekeeleys:playlist:4KFCLoqv4HIinfmA8CIdPl
    val matchResult = playlistUriPattern.matchEntire(spotifyUri)
    if(matchResult != null) {
      val (userId, playlistId)  = matchResult.destructured
      return getPlaylist(userId, playlistId)
    } else {
      return null
    }
  }

  fun getCurrentUser(): User {
    val user = spotifyApi.currentUsersProfile.build().execute()
		log.info("Retrieved current user as ${user.id}")
		return user
  }

  fun getPlaylist(userId: String, playlistId: String): Playlist {
		log.info("Getting playlist $playlistId for user $userId")
    return spotifyApi.getPlaylist(userId, playlistId)
      .fields("id,name,description,collaborative,public,owner")
      .build()
      .execute()
  }

	fun getPlaylistTracks(userId: String, playlistId: String, market: CountryCode = CountryCode.US): List<PlaylistTrack> {
		log.info("Getting tracks for playlist $playlistId")
		val limit = 100
		val firstPage = spotifyApi
			.getPlaylistsTracks(userId, playlistId)
			.limit(limit)
			.market(market)
			.build()
			.execute()

		val total = firstPage.total
		val tracks = mutableListOf(*firstPage.items)
		if(total > limit) {
			val lastPage = Math.ceil((total.toDouble() - limit) / limit).toInt()
			for (i in 1.rangeTo(lastPage)) {
				val page = spotifyApi
					.getPlaylistsTracks(userId, playlistId)
					.limit(limit)
					.offset(i * limit)
					.market(market)
					.build()
					.execute()

				tracks.addAll(page.items)
			}
		}
		return tracks.filter { it.track.id != null }
	}

	fun getArtistsInPlaylist(tracks: List<PlaylistTrack>): Map<String, Artist> {
		val artistIds = tracks.flatMap { pt -> pt.track.artists.map { artist -> artist.id } }.distinct()
		log.info("Getting ${artistIds.size} artist details for playlist")
		return artistIds.chunked(50).flatMap { ids ->
			val resp = spotifyApi.getSeveralArtists(*ids.toTypedArray()).build().execute()
			resp.filterNotNull().map { artist ->
				artist.id to artist
			}
		}.toMap()
	}

	fun getAlbumsInPlaylist(tracks: List<PlaylistTrack>): Map<String, Album> {
		val albumIds = tracks.map { it.track.album.id }.distinct()
		log.info("Getting ${albumIds.size} album details for playlist")
		return albumIds.chunked(20).flatMap { ids ->
			val resp = spotifyApi.getSeveralAlbums(*ids.toTypedArray()).build().execute()
			resp.filterNotNull().map { album ->
				album.id to album
			}
		}.toMap()
	}

	fun getAudioFeaturesInPlaylist(tracks: List<PlaylistTrack>): Map<String, AudioFeatures> {
		log.info("Getting audio features for ${tracks.size} tracks in playlist")
		return tracks.chunked(100).flatMap { chunk ->
			val ids = chunk.map { it.track.id }
			val resp = spotifyApi.getAudioFeaturesForSeveralTracks(*ids.toTypedArray()).build().execute()
			resp.filterNotNull().map { it.id to it }
		}.toMap()
	}

	fun createPlaylist(playlistName: String, description: String, trackUris: List<String>, public: Boolean = true) {
    val userId = getCurrentUser().id

		log.info("Creating playlist '$playlistName' for user $userId with ${trackUris.size} tracks")

    val playlist = spotifyApi
      .createPlaylist(userId, playlistName)
      .description(description)
      .public_(public)
      .build()
      .execute()

    trackUris.chunked(20).forEach { chunk ->
      log.debug("Adding ${chunk.size} tracks to playlist ${playlistName}")
			spotifyApi.addTracksToPlaylist(userId, playlist.id, chunk.toTypedArray()).build().execute()
		}
	}
}
