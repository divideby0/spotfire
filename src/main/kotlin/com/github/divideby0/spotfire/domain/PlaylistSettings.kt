package com.github.divideby0.spotfire.domain

data class PlaylistSettings(
  val minTracksBetweenArtistRepeat: Int = 10,
  val minimumSameKeyStreak: Int?,
  val maximumSameKeyStreak: Int?,
  val maximumKeyChangeTypeStreak: Int?,
  val allowExplicit: Boolean = true,
  val maximumTempoChange: Double?,
  val maximumEnergyChange: Double?
)