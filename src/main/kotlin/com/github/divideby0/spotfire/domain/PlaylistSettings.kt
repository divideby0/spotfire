package com.github.divideby0.spotfire.domain

data class PlaylistSettings(
  val mimimumTracksBetweenSameArtist: Int = 10,
  val minimumSameKeyStreak: Int = 0,
  val maximumSameKeyStreak: Int = 4,
  val maximumKeyChangeTypeStreak: Int = 2,
  val allowExplicit: Boolean = true,
  val maximumTempoChange: Double = 15.0,
  val maximumEnergyChange: Double = 0.3
)