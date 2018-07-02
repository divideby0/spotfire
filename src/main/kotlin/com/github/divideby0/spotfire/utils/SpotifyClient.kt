package com.github.divideby0.spotfire.utils

import com.neovisionaries.i18n.CountryCode
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.specification.*

class SpotifyClient(
	val spotifyApiBuilder: SpotifyApi.Builder = SpotifyApi.Builder(),
	val accessToken: String,
	val spotifyApi: SpotifyApi = spotifyApiBuilder.setAccessToken(accessToken).build()
) {
  val playlistUriPattern = Regex("spotify:user:([^:]+):playlist:(\\w+)")

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
    return spotifyApi.currentUsersProfile.build().execute()
  }

  fun getPlaylist(userId: String, playlistId: String): Playlist {
    return spotifyApi.getPlaylist(userId, playlistId)
      .fields("id,name,description,collaborative,public,owner")
      .build()
      .execute()
  }

	fun getPlaylistTracks(userId: String, playlistId: String, market: CountryCode = CountryCode.US): List<PlaylistTrack> {
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
		return artistIds.chunked(50).flatMap { ids ->
			val resp = spotifyApi.getSeveralArtists(*ids.toTypedArray()).build().execute()
			resp.filterNotNull().map { artist ->
				artist.id to artist
			}
		}.toMap()
	}

	fun getAlbumsInPlaylist(tracks: List<PlaylistTrack>): Map<String, Album> {
		val albumIds = tracks.map { it.track.album.id }.distinct()
		return albumIds.chunked(20).flatMap { ids ->
			val resp = spotifyApi.getSeveralAlbums(*ids.toTypedArray()).build().execute()
			resp.filterNotNull().map { album ->
				album.id to album
			}
		}.toMap()
	}

	fun getAudioFeaturesInPlaylist(tracks: List<PlaylistTrack>): Map<String, AudioFeatures> {
		return tracks.chunked(100).flatMap { chunk ->
			val ids = chunk.map { it.track.id }
			val resp = spotifyApi.getAudioFeaturesForSeveralTracks(*ids.toTypedArray()).build().execute()
			resp.filterNotNull().map { it.id to it }
		}.toMap()
	}

	fun createPlaylist(playlistName: String, description: String, trackUris: List<String>) {
    val userId = getCurrentUser().id

    val playlist = spotifyApi
      .createPlaylist(userId, playlistName)
      .description(description)
      .public_(true)
      .build()
      .execute()

    trackUris.chunked(20).forEach { chunk ->
      println("Adding ${chunk.size} tracks to playlist ${playlistName}")
			spotifyApi.addTracksToPlaylist(userId, playlist.id, chunk.toTypedArray()).build().execute()
		}
	}
}
