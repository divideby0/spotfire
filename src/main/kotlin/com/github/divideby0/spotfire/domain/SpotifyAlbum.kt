package com.github.divideby0.spotfire.domain

import com.wrapper.spotify.enums.AlbumType
import java.time.Duration
import java.time.LocalDate

data class SpotifyAlbum(
	val spotifyId: String,
	val artists: List<SpotifyArtist>,
	val name: String,
	val albumType: AlbumType,
	val genres: List<String>,
	val label: String,
	val popularity: Int,
	val releaseDate: LocalDate?,
	val yearsSinceRelease: Float? = if(releaseDate != null) {
		releaseDate.until(LocalDate.now()).days.toFloat() / 365
	} else {
		null
	}
) {
//	constructor(album: Album, artistMap: Map<String, SpotifyArtist>): this(
//		spotifyId = album.id,
//		artists = album.artists.mapNotNull { artistMap[it.id] },
//		name = album.name,
//		albumType = album.albumType,
//		genres = album.genres.toList(),
//		label = album.label,
//		popularity = album.popularity,
//		releaseDate =  LocalDate.parse(when(album.releaseDatePrecision) {
//			DAY -> album.releaseDate
//			MONTH -> "${album.releaseDate}-01"
//			YEAR -> "${album.releaseDate}-01-01"
//			else -> "1900-01-01"
//		})
//	)

	override fun equals(other: Any?) = if(other is SpotifyAlbum) other.spotifyId == spotifyId else false
	override fun hashCode() = spotifyId.hashCode()
}
