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
	val trackKey: Key,
	val trackKeyConfidence: Float,
	val startKey: Key,
	val startKeyConfidence: Float,
	val endKey: Key,
	val endKeyConfidence: Float,

	val trackTempo: Float,
	val trackTempoConfidence: Float,
	val startTempo: Float,
	val startTempoConfidence: Float,
	val endTempo: Float,
	val endTempoConfidence: Float,

	val timeSignature: Int,
	val acousticness: Float,
	val danceability: Float,
	val energy: Float,
	val instrumentalness: Float,
	val liveness: Float,
	val loudness: Float,
	val speechiness: Float,
	val valence: Float) {

	override fun equals(other: Any?) = if(other is SpotifyTrack) other.spotifyId == spotifyId else false
	override fun hashCode() = spotifyId.hashCode()

	val artistsString = artists.joinToString(" + ") { it.name }
	val simpleName = "$artistsString - $name"

	override fun toString(): String {
		val artistStr = artists.joinToString(" + ") { it.name }
		return "$artistStr - $name (key: $startKey->$endKey, trackTempo: $trackTempo, duration: ${duration.seconds}s)"
	}
}
