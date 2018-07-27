package com.github.divideby0.spotfire.domain

enum class KeyChangeType(val cost: Int = 0) {
    SAME_KEY(0),
    UP_FIFTH(1),
    DOWN_FIFTH(1),
    RELATIVE_MAJOR(1),
    RELATIVE_MINOR(1),
    UP_RELATIVE_MINOR_FIFTH(2),
    DOWN_RELATIVE_MINOR_FIFTH(2),
    UP_RELATIVE_MAJOR_FIFTH(2),
    DOWN_RELATIVE_MAJOR_FIFTH(3),
    UP_SECOND(5),
    UNKNOWN(8);
}