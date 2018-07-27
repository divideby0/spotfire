package com.github.divideby0.spotfire.solver

import com.github.divideby0.spotfire.domain.PlaylistTrack

class PlaylistTrackDifficultyComparator : Comparator<PlaylistTrack> {
  override fun compare(o1: PlaylistTrack, o2: PlaylistTrack): Int {
    o1.position?.let { o1p ->
      o2.position?.let { o2p ->
        return -(o1p.compareTo(o2p))
      } ?: run { return -1 }
    } ?: run { return 1 }
  }
}