package com.github.divideby0.spotfire.domain

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty
import org.optaplanner.core.api.domain.solution.PlanningScore
import org.optaplanner.core.api.domain.solution.PlanningSolution
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty
import org.optaplanner.core.api.domain.solution.drools.ProblemFactProperty
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider
import org.optaplanner.core.api.score.buildin.bendable.BendableScore

@PlanningSolution
class Playlist() {
	@ProblemFactCollectionProperty
	lateinit var artists: List<SpotifyArtist>

	@ProblemFactCollectionProperty
	lateinit var albums: List<SpotifyAlbum>

  @ProblemFactCollectionProperty
  lateinit var keys: List<Key>

  @ProblemFactCollectionProperty
	lateinit var tracks: List<SpotifyTrack>

  @ProblemFactProperty
  lateinit var settings: PlaylistSettings

	@ValueRangeProvider(id = "trackRange")
	fun getTrackRange() = listOf(*tracks.toTypedArray(), null)

	@PlanningEntityCollectionProperty
	lateinit var assignments: List<PlaylistAssignment>

	@PlanningScore(bendableHardLevelsSize = 4, bendableSoftLevelsSize = 4)
	lateinit var score: BendableScore

	constructor(settings: PlaylistSettings,
              artists: List<SpotifyArtist>,
              albums: List<SpotifyAlbum>,
              keys: List<Key>,
              tracks: List<SpotifyTrack>,
              assignments: List<PlaylistAssignment>): this() {
		this.settings = settings
    this.artists = artists
		this.albums = albums
    this.keys = keys
		this.tracks = tracks
    this.assignments = assignments
	}
}
