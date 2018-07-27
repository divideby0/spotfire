package com.github.divideby0.spotfire.domain

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty
import org.optaplanner.core.api.domain.solution.PlanningScore
import org.optaplanner.core.api.domain.solution.PlanningSolution
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty
import org.optaplanner.core.api.domain.solution.drools.ProblemFactProperty
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider
import org.optaplanner.core.api.score.buildin.bendable.BendableScore

@PlanningSolution
class SpotfirePlaylist() {
	@ProblemFactCollectionProperty
	lateinit var artists: List<SpotifyArtist>

	@ProblemFactCollectionProperty
	lateinit var albums: List<SpotifyAlbum>

	@ProblemFactCollectionProperty
	lateinit var keys: List<Key>

	@ProblemFactCollectionProperty
	@ValueRangeProvider(id = "tracks")
	lateinit var tracks: List<SpotifyTrack>

	@ProblemFactProperty
	lateinit var settings: PlaylistSettings

	@PlanningEntityCollectionProperty
	lateinit var playlistTracks: List<PlaylistTrack>

    @PlanningScore(bendableHardLevelsSize = 4, bendableSoftLevelsSize = 5)
	lateinit var score: BendableScore

	constructor(settings: PlaylistSettings,
              artists: List<SpotifyArtist>,
              albums: List<SpotifyAlbum>,
              keys: List<Key>,
              tracks: List<SpotifyTrack>): this() {
        this.settings = settings
        this.artists = artists
        this.albums = albums
        this.keys = keys
        this.tracks = tracks.shuffled()
        this.playlistTracks = this.tracks.mapIndexed { i, _ -> PlaylistTrack(i) }
//        this.firstTrack
	}
}
