package com.github.divideby0.spotfire

import com.github.divideby0.spotfire.proto.SpotifyProtos
import com.github.divideby0.spotfire.spotify.SpotifyIO
import com.github.divideby0.spotfire.spotify.SpotifyProtoUtils
import com.wrapper.spotify.SpotifyHttpManager
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SpotifySandboxSpec: Spek({
    given("Spotify credentials") {
        val clientId = System.getenv("SPOTIFY_CLIENT_ID") ?: "4a5f6f0c2e864e60aa72aefb411e83a8"
        val clientSecret = System.getenv("SPOTIFY_CLIENT_SECRET")
        val refreshToken = System.getenv("SPOTIFY_REFRESH_TOKEN") ?: "AQDNA4J8F4A9U9kBgEuu5J3m6S4KqfDsds9w_FbEFw_TavUSMa5i0Ml_cUCTHbWyNyECgfy9OTRTSLQEiRruHbneFrClMlS2tfPm2SJSpt5zew9KaNXeAvrLLRLuYlICGcQ"
        val userId = "cedric.hurst"
        val playlistId = "30xBsx01pRYKdMhY9Zzby5"


        val start = System.currentTimeMillis()
        val io = SpotifyIO(
            clientId = clientId,
            clientSecret = clientSecret
        )
        val api = io.getApiFromRefreshToken(refreshToken)

        SpotifyHttpManager.Builder().build()

        val filename = "playlist-$playlistId.spotify.proto"

        on("Spotify API initialization") {
            val playlist = io.getPlaylistProto(api, userId, playlistId)

            val took = System.currentTimeMillis()-start
            val file = File(filename)
            val fos = FileOutputStream(file)
            it("should get audio analysis") {
                assert(playlist != null)
                println("Took ${took}ms")
                playlist.writeTo(fos)
                println("Size: ${file.length()/1000}kb")
            }
        }

        on("Deserialization") {
            val file = File(filename)
            val fis = FileInputStream(file)
            val playlist = SpotifyProtos.Playlist.parseFrom(fis)
            val problem = SpotifyProtoUtils.toSpotfirePlaylist(playlist)
            it("should have a playlist") {
                println(problem)
            }
        }
    }
})