package com.eme22.bolo.controller;

import com.eme22.bolo.dto.AuthRequest;
import com.eme22.bolo.dto.AuthResponse;
import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.HashSet;
import java.util.Arrays;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @ConfigProperty(name = "auth.username")
    String adminUsername;

    @ConfigProperty(name = "auth.password")
    String adminPassword;

    @ConfigProperty(name = "auth.jwt-issuer")
    String issuer;

    @ConfigProperty(name = "auth.jwt-secret")
    String secret;

    @POST
    @Path("/login")
    public Response login(AuthRequest request) {
        if (adminUsername.equals(request.getUsername()) && adminPassword.equals(request.getPassword())) {
            String token = Jwt.issuer(issuer)
                    .upn(request.getUsername())
                    .groups(new HashSet<>(Arrays.asList("admin")))
                    .signWithSecret(secret);
            return Response.ok(new AuthResponse(token)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
