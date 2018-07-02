package com.github.divideby0.spotfire.domain

import com.wrapper.spotify.model_objects.specification.Artist

data class SpotifyArtist(
	val spotifyId: String,
	val name: String,
	val popularity: Int
) {
	constructor(artist: Artist): this(artist.id, artist.name, artist.popularity)

	override fun equals(other: Any?) = if(other is SpotifyArtist) other.spotifyId == spotifyId else false
	override fun hashCode() = spotifyId.hashCode()
}
