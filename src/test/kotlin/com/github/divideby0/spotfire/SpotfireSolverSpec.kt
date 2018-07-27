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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SpotfireSolverSpec: Spek({
    given("Solve ") {
        val clientId = System.getenv("SPOTIFY_CLIENT_ID")
        val clientSecret = System.getenv("SPOTIFY_CLIENT_SECRET")
        val refreshToken = System.getenv("SPOTIFY_REFRESH_TOKEN")

        val io = SpotifyIO(
            clientId = clientId,
            clientSecret = clientSecret
        )
//        val playlistName = "keeping-it-1000-2018-07-21"
        val playlistName = "heat-makes-the-landscape-shimmer"
//        val playlistName = "cedrics-daily-mix-2018-07-02"
        val service = SpotfireService()

        val fis = this.javaClass.getResourceAsStream("/$playlistName.spotify.proto")
        val playlist = SpotifyProtos.Playlist.parseFrom(fis)

        on("Should solve") {
            val solution = service.solvePlaylist(playlist, PlaylistSettings())
            it("should get audio analysis") {
                solution.playlistTracks.forEach {
                    println("${it.positionString} ${it.keyChange?.type} -> ${it.track?.toString()}")
                }
            }
            it("should save playlist") {
                val destName = "Test ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
                val api = io.getApiFromRefreshToken(refreshToken)
                io.savePlaylist(api, solution, destName, "")
            }
        }
    }
})