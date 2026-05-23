package com.eme22.bolo.controller;

import com.eme22.bolo.Bot;
import com.eme22.bolo.dto.ServerDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/servers")
@Produces(MediaType.APPLICATION_JSON)
public class ServerResource {

    @Inject
    Bot bot;

    @GET
    @RolesAllowed({"admin"})
    public List<ServerDTO> getServers() {
        return bot.getJDA().getGuilds().stream()
                .map(guild -> new ServerDTO(guild.getId(), guild.getName()))
                .collect(Collectors.toList());
    }
}
