package com.github.divideby0.spotfire

import com.github.divideby0.spotfire.domain.PlaylistSettings
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

class SpotfireSolverSpec: Spek({
    given("Spotify credentials") {
        val playlistId = "30xBsx01pRYKdMhY9Zzby5"
        val service = SpotfireService()


        val start = System.currentTimeMillis()
        val filename = "playlist-$playlistId.spotify.proto"

        val file = File(filename)
        val fis = FileInputStream(file)
        val playlist = SpotifyProtos.Playlist.parseFrom(fis)

        on("Should solve") {
            val solution = service.solvePlaylist(playlist, PlaylistSettings())
            it("should get audio analysis") {
                println(solution)
            }
        }
    }
})