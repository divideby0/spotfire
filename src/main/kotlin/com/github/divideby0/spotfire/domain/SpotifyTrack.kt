package com.github.divideby0.spotfire.domain

import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.variable.PlanningVariable
import java.time.Duration

@PlanningEntity
data class SpotifyTrack(
	// track properties
	val spotifyId: String,
	val spotifyUri: String,
	val artists: List<SpotifyArtist>,
	val album: SpotifyAlbum,
	val name: String,
	val trackNumber: Int,
	val discNumber: Int,
	val duration: Duration,
	val explicit: Boolean,
	val popularity: Int,
	val previewUrl: String?,

	// audio features
	val key: Key,
	val tempo: Float,
	val timeSignature: Int,
	val acousticness: Float,
	val danceability: Float,
	val energy: Float,
	val instrumentalness: Float,
	val liveness: Float,
	val loudness: Float,
	val speechiness: Float,
	val valence: Float,

	var position: Int? = null,

	@PlanningVariable(valueRangeProviderRefs = ["tracksWithNull"], nullable = true)
	var nextTrack: SpotifyTrack? = null
) {
//	constructor(track: Track, features: AudioFeatures, keyMap: Map<Pair<ChromaticNote, Modality>, Key>, artistMap: Map<String, SpotifyArtist>, albumMap: Map<String, SpotifyAlbum>): this(
//		spotifyId = track.uri,
//		spotifyUri = track.uri,
//		artists = track.artists.mapNotNull { artistMap[it.id] },
//		album = albumMap.getValue(track.album.id),
//		name = track.name,
//		trackNumber = track.trackNumber,
//		discNumber = track.discNumber,
//		duration = Duration.of(track.durationMs.toLong(), ChronoUnit.MILLIS),
//		explicit = track.isExplicit,
//		popularity = track.popularity,
//		previewUrl = track.previewUrl,
//
//		key = keyMap.getValue(Pair(ChromaticNote.values()[features.key], features.mode)),
//		tempo = features.tempo,
//		timeSignature = features.timeSignature,
//		acousticness = features.acousticness,
//		danceability = features.danceability,
//		energy = features.energy,
//		instrumentalness = features.instrumentalness,
//		liveness = features.liveness,
//		loudness = features.loudness,
//		speechiness = features.speechiness,
//		valence = features.valence
//	)

	override fun equals(other: Any?) = if(other is SpotifyTrack) other.spotifyId == spotifyId else false
	override fun hashCode() = spotifyId.hashCode()

	val artistsString = artists.joinToString(" + ") { it.name }
	val simpleName = "$artistsString - $name"

	override fun toString(): String {
		val artistStr = artists.joinToString(" + ") { it.name }
		return "$artistStr - $name (key: $key, tempo: $tempo, duration: ${duration.seconds}s)"
	}
}
