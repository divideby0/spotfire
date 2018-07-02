package com.github.divideby0.playlister.domain

enum class ChromaticNote {
	C,
	C_SHARP,
	D,
	D_SHARP,
	E,
	F,
	F_SHARP,
	G,
	G_SHARP,
	A,
	A_SHARP,
	B;

	val label = this.name.replace("_SHARP", "#")
}