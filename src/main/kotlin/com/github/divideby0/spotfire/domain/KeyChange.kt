package com.github.divideby0.spotfire.domain

data class KeyChange(
    val fromKey: Key,
    val toKey: Key,
    val type: KeyChangeType,
    val cost: Int
)