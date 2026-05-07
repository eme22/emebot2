package com.eme22.bolo.controller;

import com.eme22.bolo.model.ServerStats;
import com.eme22.bolo.model.Stats;
import com.eme22.bolo.repository.ServerStatsRepository;
import com.eme22.bolo.repository.StatsRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/stats")
@Produces(MediaType.APPLICATION_JSON)
public class StatsResource {

    private final ServerStatsRepository serverStatsRepository;
    private final StatsRepository statsRepository;

    @Inject
    public StatsResource(ServerStatsRepository serverStatsRepository, StatsRepository statsRepository) {
        this.serverStatsRepository = serverStatsRepository;
        this.statsRepository = statsRepository;
    }

    @GET
    @Path("/server/{id}")
    public ServerStats getServerStats(@PathParam("id") Long id) {
        return serverStatsRepository.findByIdOptional(id).orElse(null);
    }

    @GET
    @Path("/server/all")
    public List<ServerStats> getAllServersStats() {
        return serverStatsRepository.listAll();
    }

    @GET
    @Path("/global/all")
    public List<Stats> getGlobalStats() {
        return statsRepository.listAll();
    }

    @GET
    @Path("/global/{name}")
    public Stats getGlobalStatsByName(@PathParam("name") String name) {
        return statsRepository.findByIdOptional(name).orElse(null);
    }
}

