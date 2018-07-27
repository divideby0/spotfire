package com.github.divideby0.spotfire.domain

import com.github.divideby0.spotfire.solver.PlaylistTrackDifficultyComparator
//import com.github.divideby0.spotfire.solver.TransitionUtils
import org.nield.kotlinstatistics.geometricMean
import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.variable.PlanningVariable

@PlanningEntity(difficultyComparatorClass = PlaylistTrackDifficultyComparator::class)
class PlaylistTrack {
    @PlanningVariable(valueRangeProviderRefs=["tracks"], nullable = true)
    var track: SpotifyTrack? = null
    var position: Int? = null

    var previousTrack: SpotifyTrack? = null

    var nextTrack: SpotifyTrack? = null

    var keyChange: KeyChange? = null

    var keyChangeConfidence: Double? = null

    var tempoChange: Float? = null
    var acousticnessChange: Float? = null
    var danceabilityChange: Float? = null
    var energyChange: Float? = null
    var instrumentalnessChange: Float? = null
    var livenessChange: Float? = null
    var loudnessChange: Float? = null
    var speechinessChange: Float? = null
    var valenceChange: Float? = null

    constructor()

    constructor(track: SpotifyTrack): this() {
        this.track = track
    }

    constructor(position: Int): this() {
        this.position = position
    }

    override fun equals(other: Any?) = if(other is PlaylistTrack) other.position == position else false
    override fun hashCode() = position!!.hashCode()

    val positionString: String?
        get() = position?.toString()?.padStart(4, '0')

    override fun toString() = "${track?.simpleName} ($positionString)"
}

