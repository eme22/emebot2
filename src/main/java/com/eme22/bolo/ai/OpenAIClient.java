package com.eme22.bolo.ai;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public interface OpenAIClient {

    @POST
    @Path("chat/completions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    OpenAIDTO.ChatCompletionResponse chatCompletion(
        @HeaderParam("Authorization") String authorizationHeader,
        OpenAIDTO.ChatCompletionRequest request
    );
}
