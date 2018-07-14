package com.github.divideby0.spotfire.domain

data class PlaylistSettings(
  val minTracksBetweenArtistRepeat: Int = 10,
  val minimumSameKeyStreak: Int? = null,
  val maximumSameKeyStreak: Int? = null,
  val maximumKeyChangeTypeStreak: Int? = null,
  val allowExplicit: Boolean = true,
  val maximumTempoChange: Double? = null,
  val maximumEnergyChange: Double? = 30.0
)