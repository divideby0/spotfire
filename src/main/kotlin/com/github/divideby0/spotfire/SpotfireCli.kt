package com.github.divideby0.spotfire

import com.github.divideby0.spotfire.domain.*
import com.github.divideby0.spotfire.domain.ChromaticNote
import com.github.divideby0.spotfire.domain.Key
import com.github.divideby0.spotfire.utils.SpotifyClient
import com.wrapper.spotify.enums.Modality
import org.optaplanner.core.api.solver.Solver
import org.optaplanner.core.api.solver.SolverFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.cli.*
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

object SpotfireCli {
  val log = LoggerFactory.getLogger(this.javaClass)

  @JvmStatic
  fun main(args: Array<String>) {
    val cli = CommandLineInterface("Spotify Playlister")
    val accessToken by cli.flagValueArgument("-a", "--access-token", "Spotify access token (required)")
    val sourcePlaylistUri by cli.flagValueArgument("-p", "--playlist-uri", "Spotify playlist URI (required)")
    val providedPlaylistName by cli.flagValueArgument("-n", "--playlist-name", "Destination playlist name")
    val minTracksBetweenArtistRepeat by cli.flagValueArgument("-t", "--tracks-between-artist-repeat", "Minimum tracks between artist repeat", 7) { it.toInt() }
    val allowExplicit by cli.flagValueArgument("-e", "--explicit", "Allow explicit tracks", true) { it.toBoolean() }
    val dryRun by cli.flagArgument("--dry-run", "Dry run")

    try {
      cli.parse(args)
      if (accessToken.isNullOrEmpty() or sourcePlaylistUri.isNullOrEmpty()) {
        cli.printHelp()
        exitProcess(2)
      }
    } catch (e: Exception) {
      exitProcess(1)
    }

    val spotify = SpotifyClient(accessToken = accessToken.orEmpty())

    val settings = PlaylistSettings(
      minTracksBetweenArtistRepeat = minTracksBetweenArtistRepeat,
      allowExplicit = allowExplicit,
      minimumSameKeyStreak = 2,
      maximumSameKeyStreak = 4,
      maximumEnergyChange = 0.25,
      maximumTempoChange = 20.0,
      maximumKeyChangeTypeStreak = 2
    )

    val sourcePlaylist = spotify.getPlaylist(sourcePlaylistUri.orEmpty())

    if (sourcePlaylist == null) {
      println("Could not find playlist with uri: $sourcePlaylistUri")
    } else {
      val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ssa")
      val destPlaylistName = providedPlaylistName
        ?: "${sourcePlaylist.name} (Optimized ${LocalDateTime.now().format(dateFormat)}, explicit: ${settings.allowExplicit})"

      println("Creating playlist '$destPlaylistName'")

      val keyMap = ChromaticNote.values().flatMap { note ->
        Modality.values().map { mode ->
          Pair(note, mode) to Key(note, mode)
        }
      }.toMap()

      val tracks = spotify.getPlaylistTracks(sourcePlaylist.owner.id, sourcePlaylist.id)

      val artistMap = spotify.getArtistsInPlaylist(tracks).map {
        it.key to SpotifyArtist(it.value)
      }.toMap()

      val albumMap = spotify.getAlbumsInPlaylist(tracks).map {
        it.key to SpotifyAlbum(it.value, artistMap)
      }.toMap()

      val featureMap = spotify.getAudioFeaturesInPlaylist(tracks)

      val spotifyTracks = tracks.mapNotNull { pt ->
        val track = pt.track
        val features = featureMap[track.id]
        if (features != null) {
          SpotifyTrack(track, features, keyMap, artistMap, albumMap)
        } else {
          println("Could not find features for $track")
          null
        }
      }.filter { if (settings.allowExplicit) true else !it.explicit }

      val assignments = 0.until(spotifyTracks.size).map { i -> PlaylistAssignment(i) }

      val problem = Playlist(
        settings = settings,
        artists = artistMap.values.toList(),
        albums = albumMap.values.toList(),
        keys = keyMap.values.toList(),
        tracks = spotifyTracks.shuffled(),
        assignments = assignments
      )

      val solverFactory: SolverFactory<Playlist> = SolverFactory.createFromXmlResource("com/github/divideby0/spotfire/solverConfig.xml")
      val solver: Solver<Playlist> = solverFactory.buildSolver()
      val solution = solver.solve(problem)

      val scoreDirector = solver.scoreDirectorFactory.buildScoreDirector()
      scoreDirector.workingSolution = solution

      log.info("")

      val trackUris = solution.assignments.sortedBy { it.position }.mapNotNull { it.track?.spotifyUri }

      log.info("Score: ${solution.score}")

      scoreDirector.constraintMatchTotals.sortedBy { it.scoreTotal }.forEach { mt ->
        val violationSummary = "${mt.constraintName} -> violations: ${mt.constraintMatchCount}, score impact: ${mt.scoreTotal.toShortString()}"
        log.info(violationSummary)
        mt.constraintMatchSet.sortedBy { it.score }.forEachIndexed { i, match ->
          log.debug("  - Violation $i, score impact: (${match.score})")
          match.justificationList.forEach { obj ->
            if(obj is TrackTransition) {
              obj.next.track?.let { next ->
                obj.previous.track?.let { previous ->
                  val messages = mutableListOf<String>()
                  match.constraintName.toLowerCase().let { c ->
                    if (c.contains("key")) messages.add("key: ${previous.key} -> ${next.key} (${obj.keyChangeType
                      ?: "UNKNOWN"})")
                    if (c.contains("tempo")) messages.add("tempo: ${previous.tempo} -> ${next.tempo} (${obj.tempoChange})")
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

      log.info("Playlist")
      log.info("============")
      var lastAssignment: PlaylistAssignment? = null
      solution.assignments.sortedBy { it.position }.forEach { a ->
        a.track?.let { track ->
          val sb = StringBuilder()
          sb.append("=> ")
          if(lastAssignment != null) {
            val transition = TrackTransition(lastAssignment!!, a)
            sb.append(transition)
          } else {
            sb.append(track.key)
          }
          sb.append(" =>")
          log.info(sb.toString())
        }
        log.info(a.toString())
        lastAssignment = a
      }

      log.info("")

      if(!dryRun) {
        spotify.createPlaylist(
          playlistName = destPlaylistName,
          description = "Spotfire-enhanced playlist based on ${spotify.getPlaylistUrl(sourcePlaylist)} => Score: ${solution.score}",
          trackUris = trackUris
        )
      } else {
        log.info("Skipping actual Spotify playlist creation bc dry run enabled")
      }
    }
  }
}
