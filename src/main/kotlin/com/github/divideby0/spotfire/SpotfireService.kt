package com.github.divideby0.spotfire

import com.github.divideby0.spotfire.domain.PlaylistSettings
import com.github.divideby0.spotfire.domain.SpotfirePlaylist
import com.github.divideby0.spotfire.proto.SpotifyProtos
import com.github.divideby0.spotfire.spotify.SpotifyProtoUtils
import org.optaplanner.core.api.solver.Solver
import org.optaplanner.core.api.solver.SolverFactory

class SpotfireService {
    val solverFactory: SolverFactory<SpotfirePlaylist> = SolverFactory.createFromXmlResource("com/github/divideby0/spotfire/solverConfig.xml")
    val solver: Solver<SpotfirePlaylist> = solverFactory.buildSolver()

    fun solvePlaylist(playlist: SpotifyProtos.Playlist, settings: PlaylistSettings): SpotfirePlaylist {
        val problem = solvePlaylist(SpotifyProtoUtils.toSpotfirePlaylist(playlist))
        problem.settings = settings
        return solvePlaylist(problem)
    }

    fun solvePlaylist(problem: SpotfirePlaylist): SpotfirePlaylist {
        return solver.solve(problem)
    }
}