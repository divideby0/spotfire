package com.github.divideby0.spotfire.domain

import com.github.divideby0.spotfire.proto.SpotifyProtos
import com.wrapper.spotify.model_objects.specification.Artist

data class SpotifyArtist(
	val spotifyId: String,
	val name: String,
	val popularity: Int,
	val genres: List<SpotifyGenre>
) {
//	constructor(artist: Artist): this(artist.id, artist.name, artist.popularity, artist.genres.toList())
//	constructor(artist: SpotifyProtos.Artist): this(artist.id, artist.name, artist.popularity, artist.genresList)

	override fun equals(other: Any?) = if(other is SpotifyArtist) other.spotifyId == spotifyId else false
	override fun hashCode() = spotifyId.hashCode()
}
