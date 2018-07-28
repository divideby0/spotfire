package com.github.divideby0.spotfire

import com.github.divideby0.spotfire.domain.*
import com.github.divideby0.spotfire.proto.SpotifyProtos
import com.github.divideby0.spotfire.spotify.SpotifyIO
import com.github.divideby0.spotfire.spotify.SpotifyProtoUtils
import org.optaplanner.core.api.solver.Solver
import org.optaplanner.core.api.solver.SolverFactory
import kotlinx.cli.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.system.exitProcess

object SpotfireCli {
    val log = LoggerFactory.getLogger(this.javaClass)

    @JvmStatic
    fun main(args: Array<String>) {
        val cli = CommandLineInterface("Spotify Playlister")
        val refreshTokenArg by cli.flagValueArgument("-r", "--refresh-token", "Spotify access token (required)")
        val sourcePlaylistUrlArg by cli.flagValueArgument("-p", "--playlist-uri", "Spotify playlist URI")
        val sourcePlaylistFile by cli.flagValueArgument("-f", "--playlist-file", "Source playlist file")
        val providedPlaylistName by cli.flagValueArgument("-n", "--playlist-name", "Destination playlist name")
        val minTracksBetweenArtistRepeat by cli.flagValueArgument("-t", "--tracks-between-artist-repeat", "Minimum tracks between artist repeat", 7) { it.toInt() }
        val allowExplicit by cli.flagValueArgument("-e", "--explicit", "Allow explicit tracks", true) { it.toBoolean() }
        val dryRun by cli.flagArgument("--dry-run", "Dry run")

        val clientId = System.getenv("SPOTIFY_CLIENT_ID")
        val clientSecret = System.getenv("SPOTIFY_CLIENT_SECRET")

        try {
            cli.parse(args)
        } catch (e: Exception) {
            exitProcess(1)
        }

        if (clientId.isNullOrEmpty()) {
            println("Please provide envvar for SPOTIFY_CLIENT_ID")
            exitProcess(1)
        }

        if (clientSecret.isNullOrEmpty()) {
            println("Please provide envvar for SPOTIFY_CLIENT_SECRET")
            exitProcess(1)
        }

        val refreshToken = if (refreshTokenArg.isNullOrEmpty()) {
            print("Refresh token: ")
            readLine()!!.trim()
        } else {
            refreshTokenArg!!
        }

        val io = SpotifyIO(clientId, clientSecret)
        val service = SpotfireService()

        val settings = PlaylistSettings(
            minTracksBetweenArtistRepeat = minTracksBetweenArtistRepeat,
            allowExplicit = allowExplicit,
            minimumSameKeyStreak = 2,
            maximumSameKeyStreak = 4,
            maximumEnergyChange = 0.25,
            maximumTempoChange = 20.0,
            maximumKeyChangeTypeStreak = 2
        )

        var writeFile = false

        val sourcePlaylistProtoFile = if(!sourcePlaylistFile.isNullOrEmpty() && File(sourcePlaylistFile).exists()) {
            File(sourcePlaylistFile)
        } else {
            val sourcePlaylistUrl = if(sourcePlaylistUrlArg.isNullOrEmpty()) {
                print("Spotify Playlist URI: ")
                readLine()!!.trim()
            } else {
                sourcePlaylistUrlArg!!
            }
            val playlist = io.getPlaylist(refreshToken, sourcePlaylistUrl)
            if(playlist != null) {
                val filename = "playlist-${playlist.id}-${playlist.snapshotId}.spotify.proto"
                if (File(filename).exists()) {
                    File(filename)
                } else {
                    val proto = io.getPlaylistProto(refreshToken, sourcePlaylistUrl)
                    val file = File(filename)
                    proto.writeTo(FileOutputStream(file))
                    file
                }
            } else {
                log.error("Could not find playlist for url $sourcePlaylistUrl")
                exitProcess(2)
            }
        }

        val sourcePlaylistProto = SpotifyProtos.Playlist.parseFrom(FileInputStream(sourcePlaylistProtoFile))

        val problem = SpotifyProtoUtils.toSpotfirePlaylist(sourcePlaylistProto, settings)

        val solution = service.solvePlaylist(problem)
        val scoreDirector = service.solver.scoreDirectorFactory.buildScoreDirector()
        scoreDirector.workingSolution = solution

        log.info("")

        log.info("Score: ${solution.score}")

        scoreDirector.constraintMatchTotals.sortedBy { it.scoreTotal }.forEach { mt ->
            val violationSummary = "${mt.constraintName} -> violations: ${mt.constraintMatchCount}, score impact: ${mt.scoreTotal.toShortString()}"
            log.info(violationSummary)
            mt.constraintMatchSet.sortedBy { it.score }.forEachIndexed { i, match ->
                log.debug("  - Violation $i, score impact: (${match.score})")
                match.justificationList.forEach { obj ->
                    if (obj is TrackTransition) {
                        obj.next.track?.let { next ->
                            obj.previous.track?.let { previous ->
                                val messages = mutableListOf<String>()
                                match.constraintName.toLowerCase().let { c ->
                                    if (c.contains("trackKey")) messages.add("trackKey: ${previous.trackKey} -> ${next.trackKey} (${obj.keyChangeType
                                        ?: "UNKNOWN"})")
                                    if (c.contains("trackTempo")) messages.add("trackTempo: ${previous.trackTempo} -> ${next.trackTempo} (${obj.tempoChange})")
                                    if (c.contains("energy")) messages.add("energy: ${previous.energy} -> ${next.energy} (${obj.energyChange})")
                                }
                                log.debug("    - ${messages.joinToString(", ")} => $obj")
                            }
                        }
                    } else {
                        log.debug("    - $obj")
                    }
                }
            }
        }

        log.info("")

        log.info("SpotfirePlaylist")
        log.info("============")

        val destPlaylistName = providedPlaylistName ?: "${sourcePlaylistProto.name} (Spotfired)"

        if (!dryRun) {
            io.savePlaylist(
                api = io.getApiFromRefreshToken(refreshToken),
                playlistName = destPlaylistName,
                description = "Spotfire-enhanced playlist based on ${sourcePlaylistProto.uri} => Score: ${solution.score}",
                solution = solution
            )
        } else {
            log.info("Skipping actual Spotify playlist creation bc dry run enabled")
        }
    }
}
