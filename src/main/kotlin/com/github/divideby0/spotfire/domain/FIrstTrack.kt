package com.github.divideby0.spotfire.domain

import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.variable.PlanningVariable

@PlanningEntity
data class FirstTrack(
    @PlanningVariable(valueRangeProviderRefs = ["tracks"])
    var track: SpotifyTrack
)