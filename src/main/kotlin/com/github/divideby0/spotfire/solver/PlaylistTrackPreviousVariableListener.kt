package com.github.divideby0.spotfire.solver

import com.github.divideby0.spotfire.domain.PlaylistTrack
import org.optaplanner.core.impl.domain.variable.listener.VariableListener
import org.optaplanner.core.impl.score.director.ScoreDirector

class PlaylistTrackPreviousVariableListener : VariableListener<PlaylistTrack> {
    override fun beforeEntityRemoved(scoreDirector: ScoreDirector<*>, entity: PlaylistTrack) {
//        entity.position = null
    }

    override fun afterVariableChanged(scoreDirector: ScoreDirector<*>, entity: PlaylistTrack) {
        updatePlaylistTrack(scoreDirector, entity)
    }

    override fun beforeEntityAdded(scoreDirector: ScoreDirector<*>, entity: PlaylistTrack) {
//        updatePlaylistTrack(scoreDirector, entity)
    }

    override fun afterEntityAdded(scoreDirector: ScoreDirector<*>, entity: PlaylistTrack) {
//        updatePlaylistTrack(scoreDirector, entity)
    }

    override fun afterEntityRemoved(scoreDirector: ScoreDirector<*>, entity: PlaylistTrack) {
//        entity.position = null
    }

    override fun beforeVariableChanged(scoreDirector: ScoreDirector<*>, entity: PlaylistTrack) {
//        updatePlaylistTrack(scoreDirector, entity)
    }

    private fun updatePlaylistTrack(scoreDirector: ScoreDirector<*>, currentTrack: PlaylistTrack) {
//        currentTrack.previousTrack?.position?.let { previousPosition ->
//            scoreDirector.beforeVariableChanged(currentTrack, "position")
//            println(previousPosition + 1)
//            currentTrack.position = previousPosition + 1
//            scoreDirector.afterVariableChanged(currentTrack, "position")
//        } ?: run {
//            currentTrack.position = null
//        }
    }
}