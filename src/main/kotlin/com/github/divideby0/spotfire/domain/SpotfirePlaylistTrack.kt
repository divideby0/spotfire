package com.github.divideby0.spotfire.domain

import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.variable.PlanningVariable

@PlanningEntity
class SpotfirePlaylistTrack {
    var track: SpotifyTrack? = null

    var position: Int? = null

    @PlanningVariable(valueRangeProviderRefs = ["trackRange"], nullable = true)
    var nextTrack: SpotifyTrack? = null

    constructor()

    constructor(track: SpotifyTrack): this() {
        this.track = track
    }

    override fun equals(other: Any?) = if(other is SpotfirePlaylistTrack) other.track == track else false
    override fun hashCode() = track!!.hashCode()

    val positionString = position?.toString()?.padStart(4, '0')
    override fun toString() = "${track?.simpleName} ($positionString)"
}