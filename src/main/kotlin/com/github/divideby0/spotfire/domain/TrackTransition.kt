package com.github.divideby0.spotfire.domain

import com.github.divideby0.spotfire.domain.KeyChangeType.*
import com.wrapper.spotify.enums.Modality.*

class TrackTransition(
  val previous: PlaylistAssignment,
  val next: PlaylistAssignment
) {
  val previousPosition = previous.position
  val nextPosition = next.position
  val previousKey = previous.track!!.key
  val nextKey = next.track!!.key

  val tempoChange = next.track!!.tempo - previous.track!!.tempo
  val energyChange = next.track!!.energy - previous.track!!.energy

  val keyChangeType: KeyChangeType?

  init {
    keyChangeType = if(previousKey == nextKey) {
      SAME_KEY
    } else {
      val nextNote = previousKey.scale.indexOf(nextKey.rootNote)
      if (previousKey.mode == nextKey.mode) {
        when (nextNote) {
          4 -> UP_FIFTH
          3 -> DOWN_FIFTH
          1 -> UP_SECOND
          else -> null
        }
      } else if(previousKey.mode == MAJOR && nextKey.mode == MINOR) {
        when(nextNote) {
          5 -> RELATIVE_MINOR
          2 -> UP_RELATIVE_MINOR_FIFTH
          1 -> DOWN_RELATIVE_MINOR_FIFTH
          else -> null
        }
      } else if (previousKey.mode == MINOR && nextKey.mode == MAJOR) {
        when(nextNote) {
          2 -> RELATIVE_MAJOR
          6 -> UP_RELATIVE_MAJOR_FIFTH
          5 -> DOWN_RELATIVE_MAJOR_FIFTH
          else -> null
        }
      } else {
        null
      }
    }
  }

  override fun toString() = "keyChange: $previousKey -> $nextKey (${keyChangeType ?: "UNKNOWN"}), tempoChange: $tempoChange, energyChange: $energyChange, prev: ${previousPosition.toString().padStart(4, '0')}, next: ${nextPosition.toString().padStart(4, '0')}"
}