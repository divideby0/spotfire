package com.github.divideby0.spotfire.domain

import com.github.divideby0.spotfire.solver.PlaylistTrackDifficultyComparator
import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.variable.PlanningVariable

@Deprecated("used only by old score rules")
@PlanningEntity(difficultyComparatorClass = PlaylistTrackDifficultyComparator::class)
class PlaylistAssignment: Comparable<PlaylistAssignment> {
	var position: Int = -1

	@PlanningVariable(valueRangeProviderRefs = ["trackRange"], nullable = true)
	var track: SpotifyTrack? = null

	constructor()

	constructor(position: Int): this() {
		this.position = position
	}

	override fun equals(other: Any?) = if(other is PlaylistAssignment) other.position == position else false
	override fun hashCode() = position
	override fun compareTo(other: PlaylistAssignment) = position.compareTo(other.position)

	override fun toString() = "${position.toString().padStart(4, '0')} - ${track?.simpleName}"
}
