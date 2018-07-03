package com.github.divideby0.spotfire.domain

enum class KeyChangeType(val penalty: Int = 0) {
  SAME_KEY(0),
  UP_FIFTH(0),
  DOWN_FIFTH(0),
  RELATIVE_MAJOR(0),
  RELATIVE_MINOR(0),
  UP_RELATIVE_MINOR_FIFTH(-1),
  DOWN_RELATIVE_MINOR_FIFTH(-1),
  UP_RELATIVE_MAJOR_FIFTH(-1),
  DOWN_RELATIVE_MAJOR_FIFTH(-1),
  UP_SECOND(-2);
}