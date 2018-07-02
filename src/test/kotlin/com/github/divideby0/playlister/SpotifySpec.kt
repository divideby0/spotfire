package com.github.divideby0.playlister

import com.github.divideby0.playlister.utils.SpotifyClient
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.on

class SpotifySpec : Spek({
	given("a spotify spotifyApi") {
    val clientId = System.getenv("SPOTIFY_CLIENT_ID")
    val clientSecret = System.getenv("SPOTIFY_CLIENT_SECRET")
    val spotifyUtils = SpotifyClient(clientId, clientSecret)

		val userId = "jcolliermusic"
		val playlistId = "4kdrAVuPxDdfwOnchhexK1"

		on("a playlist request") {
			val tracks = spotifyUtils.getPlaylistTracks(userId, playlistId)
      tracks.forEach {  }
		}
	}
})
