package com.eme22.bolo.controller;

import com.eme22.bolo.model.CommandLog;
import com.eme22.bolo.repository.CommandLogRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/logs")
@Produces(MediaType.APPLICATION_JSON)
public class LogResource {

    @Inject
    CommandLogRepository commandLogRepository;

    @GET
    @RolesAllowed({"admin"})
    public List<CommandLog> getLogs(@QueryParam("server") String serverId) {
        if (serverId != null && !serverId.isEmpty()) {
            return commandLogRepository.list("server", serverId);
        }
        return commandLogRepository.listAll();
    }
}
