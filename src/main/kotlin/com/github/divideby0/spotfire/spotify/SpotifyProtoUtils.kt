package com.github.divideby0.spotfire.spotify

import com.github.divideby0.spotfire.domain.*
import com.github.divideby0.spotfire.domain.SpotfirePlaylist
import com.github.divideby0.spotfire.proto.SpotifyProtos
import com.wrapper.spotify.enums.AlbumType
import com.wrapper.spotify.enums.Modality
import com.wrapper.spotify.enums.ReleaseDatePrecision
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysis
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisMeasure
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSection
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisTrack
import com.wrapper.spotify.model_objects.specification.*
import org.nield.kotlinstatistics.standardDeviation
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object SpotifyProtoUtils {
    val log = LoggerFactory.getLogger(this.javaClass)

    val keyMap = ChromaticNote.values().flatMap { note ->
        Modality.values().map { mode ->
            Pair(note, mode) to Key(note, mode)
        }
    }.toMap()

    fun toAlbumProto(album: Album): SpotifyProtos.Album {
        val b = SpotifyProtos.Album.newBuilder()
        b.id = album.id
        b.uri = album.uri
        b.name = album.name
        b.addAllArtistIds(album.artists.map { it.id })
        b.albumType = SpotifyProtos.AlbumType.forNumber(album.albumType.ordinal)
        b.label = album.label
        b.addAllGenres(album.genres.toList())
        b.popularity = album.popularity
        b.releaseDate = album.releaseDate
        b.releaseDatePrecision = SpotifyProtos.ReleaseDatePrecision.forNumber(album.releaseDatePrecision.ordinal)
        return b.build()
    }

    fun toArtistProto(artist: Artist): SpotifyProtos.Artist {
        val b = SpotifyProtos.Artist.newBuilder()
        b.id = artist.id
        b.name = artist.name
        b.popularity = artist.popularity
        b.addAllGenres(artist.genres.toList())
        return b.build()
    }

    fun toMode(modality: Modality): SpotifyProtos.Mode {
        return SpotifyProtos.Mode.forNumber(modality.ordinal)
    }

    fun toAudioAnalysisMeasureProto(measure: AudioAnalysisMeasure): SpotifyProtos.AudioAnalysisMeasure {
        val b = SpotifyProtos.AudioAnalysisMeasure.newBuilder()
        b.confidence = measure.confidence
        b.duration = measure.duration
        b.start = measure.start
        return b.build()
    }

    fun toAudioAnalysisSectionProto(section: AudioAnalysisSection): SpotifyProtos.AudioAnalysisSection {
        val b = SpotifyProtos.AudioAnalysisSection.newBuilder()
        b.measure = toAudioAnalysisMeasureProto(section.measure)

        val d = SpotifyProtos.AudioAnalysisDetails.newBuilder()
        d.loudness = section.loudness
        d.tempo = section.tempo
        d.tempoConfidence = section.tempoConfidence
        d.key = section.key
        d.keyConfidence = section.keyConfidence
        d.mode = toMode(section.mode)
        d.modeConfidence = section.modeConfidence
        d.timeSignature = section.timeSignature
        d.timeSignatureConfidence = section.timeSignatureConfidence

        b.details = d.build()
        return b.build()
    }

    fun toAudioAnalysisDetails(trackAnalysis: AudioAnalysisTrack): SpotifyProtos.AudioAnalysisDetails {
        val b = SpotifyProtos.AudioAnalysisDetails.newBuilder()
        b.loudness = trackAnalysis.loudness
        b.tempo = trackAnalysis.tempo
        b.tempoConfidence = trackAnalysis.tempoConfidence
        b.key = trackAnalysis.key
        b.keyConfidence = trackAnalysis.keyConfidence
        b.mode = toMode(trackAnalysis.mode)
        b.modeConfidence = trackAnalysis.modeConfidence
        b.timeSignature = trackAnalysis.timeSignature
        b.timeSignatureConfidence = trackAnalysis.timeSignatureConfidence
        return b.build()
    }

    fun toAudioAnalysisProto(analysis: AudioAnalysis): SpotifyProtos.AudioAnalysis {
        val b = SpotifyProtos.AudioAnalysis.newBuilder()
        val t = analysis.track
        if(t != null) {
            b.numSamples = t.numSamples
            b.durationSeconds = t.duration
            b.offsetSeconds = t.offsetSeconds
            b.windowSeconds = t.windowSeconds
            b.sampleRate = t.analysisSampleRate
            b.channels = t.analysisChannels
            b.endOfFadeIn = t.endOfFadeIn
            b.startOfFadeOut = t.startOfFadeOut
            b.details = toAudioAnalysisDetails(t)
            b.addAllSections(analysis.sections.map(::toAudioAnalysisSectionProto))
        } else {
            log.warn("No track analysis for track")
        }

        return b.build()
    }

    fun toAudioFeaturesProto(features: AudioFeatures): SpotifyProtos.AudioFeatures {
        val b = SpotifyProtos.AudioFeatures.newBuilder()
        b.acousticness = features.acousticness
        b.danceability = features.danceability
        b.energy = features.energy
        b.instrumentalness = features.instrumentalness
        b.speechiness = features.speechiness
        b.valence = features.valence
        b.liveness = features.liveness
        return b.build()
    }

    fun toTrackProto(track: Track, features: AudioFeatures?, analysis: AudioAnalysis?): SpotifyProtos.Track {
        val b = SpotifyProtos.Track.newBuilder()
        b.id = track.id
        b.uri = track.uri
        b.name = track.name
        b.albumId = track.album.id
        b.addAllArtistIds(track.artists.map { it.id })
        b.trackNumber = track.trackNumber
        b.discNumber = track.discNumber
        b.durationMs = track.durationMs
        b.explicit = track.isExplicit
        b.popularity = track.popularity
        if(track.previewUrl != null) {
            b.previewUrl = track.previewUrl
        }
        if(features != null) {
            b.features = toAudioFeaturesProto(features)
        }
        if(analysis != null) {
            b.analysis = toAudioAnalysisProto(analysis)
        }
        return b.build()
    }

    fun toImageProto(image: Image): SpotifyProtos.Image {
        val b = SpotifyProtos.Image.newBuilder()
        b.url = image.url
        if(image.height != null) {
            b.height = image.height
        }
        if(image.width != null) {
            b.width = image.width
        }
        return b.build()
    }

    fun toUserProto(user: User): SpotifyProtos.User {
        val b = SpotifyProtos.User.newBuilder()
        b.id = user.id
        b.uri = user.uri
        if(user.displayName != null) {
            b.displayName = user.displayName
        }
        b.followers = user.followers.total
        b.addAllImages(user.images.map(::toImageProto))
        if(user.email != null) {
            b.email = user.email
        }
        return b.build()
    }

    fun getKey(details: SpotifyProtos.AudioAnalysisDetails): Key {
        val rootNote = ChromaticNote.values()[details.key]
        val mode = Modality.keyOf(details.mode.ordinal)
        return keyMap.getValue(Pair(rootNote, mode))
    }

    fun getAlternativeSection(trackDetails: SpotifyProtos.AudioAnalysisDetails, section: SpotifyProtos.AudioAnalysisSection?): SpotifyProtos.AudioAnalysisDetails {
        return if(section == null) {
            trackDetails
        } else {
            if(section.measure.duration > 10 && section.details.keyConfidence > trackDetails.keyConfidence) {
                section.details
            } else {
                trackDetails
            }
        }
    }

    fun toSpotfirePlaylist(proto: SpotifyProtos.Playlist, settings: PlaylistSettings = PlaylistSettings()): SpotfirePlaylist {
        val genreMap = proto.artistsList
            .flatMap { it.genresList }
            .distinct()
            .map {
                it to SpotifyGenre(it)
            }
            .toMap()

        val artistMap = proto.artistsList.map { pa ->
            pa.id to SpotifyArtist(
                spotifyId = pa.id,
                name = pa.name,
                popularity = pa.popularity,
                genres = pa.genresList.mapNotNull { genreMap[it] }
            )
        }.toMap()

        val albumMap = proto.albumsList.map { pa ->
            pa.id to SpotifyAlbum(
                spotifyId = pa.id,
                artists = pa.artistIdsList.mapNotNull { artistId -> artistMap[artistId] },
                name = pa.name,
                genres = pa.genresList,
                label = pa.label,
                popularity = pa.popularity,
                albumType = AlbumType.values()[pa.albumType.ordinal],
                releaseDate = LocalDate.parse(when(pa.releaseDatePrecision) {
                    ReleaseDatePrecision.DAY -> pa.releaseDate
                    ReleaseDatePrecision.MONTH -> "${pa.releaseDate}-01"
                    ReleaseDatePrecision.YEAR -> "${pa.releaseDate}-01-01"
                    else -> "1900-01-01"
                })
            )
        }.toMap()

        val tracks = proto.tracksList.map { pt ->
            pt.analysis.sectionsList.map { s-> s.details.keyConfidence }

//            val keyConfidenceTolerance = pt.analysis.sectionsList
//                .map { it.details.keyConfidence }.standardDeviation()
//                .div(2)

//            val keyConfidenceTolerance = 0

//            val minKeyConfidence = pt.analysis.details.keyConfidence - keyConfidenceTolerance
            val startDetails = getAlternativeSection(pt.analysis.details, pt.analysis.sectionsList.firstOrNull())
            val endDetails = getAlternativeSection(pt.analysis.details, pt.analysis.sectionsList.lastOrNull())

            SpotifyTrack(
                spotifyId = pt.id,
                spotifyUri = pt.uri,
                artists = pt.artistIdsList.mapNotNull { artistMap[it] },
                album = albumMap[pt.albumId]!!,
                name = pt.name,
                trackNumber = pt.trackNumber,
                discNumber = pt.discNumber,
                duration = Duration.of(pt.durationMs.toLong(), ChronoUnit.MILLIS),
                explicit = pt.explicit,
                popularity = pt.popularity,
                previewUrl = pt.previewUrl,

                trackKey = getKey(pt.analysis.details),
                trackKeyConfidence = pt.analysis.details.keyConfidence,
                startKey = getKey(startDetails),
                startKeyConfidence = startDetails.keyConfidence,
                endKey = getKey(endDetails),
                endKeyConfidence = endDetails.keyConfidence,

                trackTempo = pt.analysis.details.tempo,
                trackTempoConfidence = pt.analysis.details.tempoConfidence,
                startTempo = startDetails.tempo,
                startTempoConfidence = startDetails.tempoConfidence,
                endTempo = endDetails.tempo,
                endTempoConfidence = endDetails.tempoConfidence,

                timeSignature = pt.analysis.details.timeSignature,
                loudness = pt.analysis.details.loudness,
                acousticness = pt.features.acousticness,
                danceability = pt.features.danceability,
                energy = pt.features.energy,
                instrumentalness = pt.features.instrumentalness,
                liveness = pt.features.liveness,
                speechiness = pt.features.speechiness,
                valence = pt.features.valence
            )
        }

        return SpotfirePlaylist(
            settings,
            artistMap.values.toList(),
            albumMap.values.toList(),
            keyMap.values.toList(),
            tracks.shuffled()
        )
    }
}