package com.github.divideby0.spotfire

import com.github.divideby0.spotfire.domain.PlaylistSettings
import com.github.divideby0.spotfire.domain.SpotfirePlaylist
import com.github.divideby0.spotfire.proto.SpotifyProtos
import com.github.divideby0.spotfire.spotify.SpotifyProtoUtils
import com.github.divideby0.spotfire.utils.UberJarKieClassLoader
import org.optaplanner.core.api.solver.Solver
import org.optaplanner.core.api.solver.SolverFactory
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner

class SpotfireService {
    val solverFactory: SolverFactory<SpotfirePlaylist>
    val solver: Solver<SpotfirePlaylist>

    init {
        // needed to get kie to work in a fat jar as per here:
        // https://stackoverflow.com/a/47514137
        val localKieConfUrls = Reflections("META-INF", ResourcesScanner())
            .getResources(Regex(".*-kie.conf").toPattern())
            .map { ClassLoader.getSystemResource(it) }
            .toTypedArray()

        Thread.currentThread().contextClassLoader = UberJarKieClassLoader(Thread.currentThread().contextClassLoader, localKieConfUrls)
        solverFactory = SolverFactory.createFromXmlResource("com/github/divideby0/spotfire/solverConfig.xml")
        solver = solverFactory.buildSolver()
    }

    fun solvePlaylist(playlist: SpotifyProtos.Playlist, settings: PlaylistSettings): SpotfirePlaylist {
        val problem = solvePlaylist(SpotifyProtoUtils.toSpotfirePlaylist(playlist))
        problem.settings = settings
        return solvePlaylist(problem)
    }

    fun solvePlaylist(problem: SpotfirePlaylist): SpotfirePlaylist {
        return solver.solve(problem)
    }
}