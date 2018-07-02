package com.github.divideby0.playlister.domain

enum class KeyChangeType(val preferred: Boolean = false) {
  SAME_KEY(true),
  UP_FIFTH(true),
  DOWN_FIFTH(true),
  RELATIVE_MAJOR(true),
  RELATIVE_MINOR(true),
  UP_RELATIVE_MINOR_FIFTH(false),
  DOWN_RELATIVE_MAJOR_FIFTH(false);
}