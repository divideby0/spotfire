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

        val solverFactory: SolverFactory<SpotfirePlaylist> = SolverFactory.createFromXmlResource("com/github/divideby0/spotfire/solverConfig.xml")
        val solver: Solver<SpotfirePlaylist> = solverFactory.buildSolver()

        val refreshToken = if (refreshTokenArg.isNullOrEmpty()) {
            print("Refresh token: ")
            readLine()!!.trim()
        } else {
            refreshTokenArg!!
        }

        val io = SpotifyIO(clientId, clientSecret)

        val settings = PlaylistSettings(
            minTracksBetweenArtistRepeat = minTracksBetweenArtistRepeat,
            allowExplicit = allowExplicit,
            minimumSameKeyStreak = 2,
            maximumSameKeyStreak = 4,
            maximumEnergyChange = 0.25,
            maximumTempoChange = 20.0,
            maximumKeyChangeTypeStreak = 2
        )

        val sourcePlaylistProto = if(sourcePlaylistFile.isNullOrEmpty()) {
            val sourcePlaylistUrl = if (sourcePlaylistUrlArg.isNullOrEmpty()) {
                print("Spotify SpotfirePlaylist URI: ")
                readLine()!!.trim()
            } else {
                sourcePlaylistUrlArg!!
            }



            io.getPlaylistProto(refreshToken, sourcePlaylistUrl)
        } else {
            val fos = FileInputStream(File(sourcePlaylistFile))
            SpotifyProtos.Playlist.parseFrom(fos)
        }

        val playlistId = sourcePlaylistProto.id

        val filename = "playlist-$playlistId-${System.currentTimeMillis()}.spotify.proto"
        log.info("Writing source playlist to $filename")
        sourcePlaylistProto.writeTo(FileOutputStream(File(filename)))

        val problem = SpotifyProtoUtils.toSpotfirePlaylist(sourcePlaylistProto, settings)

        val solution = solver.solve(problem)
        val scoreDirector = solver.scoreDirectorFactory.buildScoreDirector()
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
