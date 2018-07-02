package com.github.divideby0.spotfire.solver

import com.github.divideby0.spotfire.domain.PlaylistAssignment

class PlaylistAssignmentDifficultComparator : Comparator<PlaylistAssignment> {
  override fun compare(o1: PlaylistAssignment?, o2: PlaylistAssignment?): Int {
    if(o1 == null) {
      return 1
    } else if (o2 == null) {
      return -1
    } else {
      return -(o1.position.compareTo(o2.position))
    }
  }
}