package com.github.divideby0.spotfire.domain

import com.wrapper.spotify.enums.Modality


data class Key(
	val rootNote: ChromaticNote,
	val mode: Modality
) {
	val scale: List<ChromaticNote> = when(mode) {
		Modality.MAJOR -> listOf(0, 2, 4, 5, 7, 9, 11)
		Modality.MINOR -> listOf(0, 2, 3, 5, 7, 8, 10)
	}.map { offset ->
		val noteIndex = (rootNote.ordinal + offset) % ChromaticNote.values().size
		ChromaticNote.values()[noteIndex]
	}

	val second = scale[1]
	val third = scale[2]
	val fifth = scale[4]
	val seventh = scale[6]

	private val modeSuffix = when(mode) {
		Modality.MAJOR -> "maj"
		Modality.MINOR -> "min"
	}

	override fun toString() = "${rootNote.label}$modeSuffix"
}
