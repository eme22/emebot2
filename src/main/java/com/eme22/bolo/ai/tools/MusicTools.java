package com.eme22.bolo.ai.tools;

import com.eme22.bolo.Bot;
import com.eme22.bolo.ai.AITool;
import com.eme22.bolo.ai.OpenAIDTO;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.audio.RequestMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.io.IOException;
import java.util.*;

@ApplicationScoped
public class MusicTools {

    @Inject
    Bot bot;

    @Produces
    @ApplicationScoped
    public AITool getNowPlayingTool() {
        return new AITool() {
            @Override
            public String getName() {
                return "get_now_playing";
            }

            @Override
            public String getDescription() {
                return "Obtiene información detallada sobre la canción que se está reproduciendo en el servidor actualmente.";
            }

            @Override
            public OpenAIDTO.Tool getDefinition() {
                return OpenAIDTO.Tool.builder()
                        .type("function")
                        .function(OpenAIDTO.FunctionDefinition.builder()
                                .name(getName())
                                .description(getDescription())
                                .parameters(OpenAIDTO.ParametersDefinition.builder()
                                        .type("object")
                                        .properties(new HashMap<>())
                                        .required(new ArrayList<>())
                                        .build())
                                .build())
                        .build();
            }

            @Override
            public String getRequiredMode() {
                return "NORMAL";
            }

            @Override
            public List<Permission> getRequiredUserPermissions() {
                return Collections.emptyList();
            }

            @Override
            public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                AudioHandler handler = bot.getPlayerManager().getAudioHandler(event.getGuild());
                if (handler == null || !handler.isMusicPlaying(event.getJDA())) {
                    return "No se está reproduciendo música en este servidor en este momento.";
                }

                try {
                    RequestMetadata rm = handler.getRequestMetadata();
                    String title = handler.getAudioPlayer().get().getTrack().getInfo().getTitle();
                    String author = handler.getAudioPlayer().get().getTrack().getInfo().getAuthor();
                    String requestedBy = rm.owner() == 0L ? "Autoplay" : rm.user().username();
                    return String.format("Canción actual en reproducción:\n- Título: %s\n- Artista/Autor: %s\n- Pedida por: %s",
                            title, author, requestedBy);
                } catch (IOException e) {
                    return "Error al intentar obtener la información de la canción actual.";
                }
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getSkipSongTool() {
        return new AITool() {
            @Override
            public String getName() {
                return "skip_song";
            }

            @Override
            public String getDescription() {
                return "Salta (omite) la canción que se está reproduciendo actualmente y pasa a la siguiente de la cola.";
            }

            @Override
            public OpenAIDTO.Tool getDefinition() {
                return OpenAIDTO.Tool.builder()
                        .type("function")
                        .function(OpenAIDTO.FunctionDefinition.builder()
                                .name(getName())
                                .description(getDescription())
                                .parameters(OpenAIDTO.ParametersDefinition.builder()
                                        .type("object")
                                        .properties(new HashMap<>())
                                        .required(new ArrayList<>())
                                        .build())
                                .build())
                        .build();
            }

            @Override
            public String getRequiredMode() {
                return "NORMAL";
            }

            @Override
            public List<Permission> getRequiredUserPermissions() {
                return Collections.emptyList();
            }

            @Override
            public String execute(MessageReceivedEvent event, Map<String, Object> arguments) throws Exception {
                AudioHandler handler = bot.getPlayerManager().getAudioHandler(event.getGuild());
                if (handler == null || !handler.isMusicPlaying(event.getJDA())) {
                    return "No hay ninguna canción reproduciéndose para poder saltar.";
                }

                String title = handler.getAudioPlayer().get().getTrack().getInfo().getTitle();
                handler.getAudioPlayer().get().stopTrack();
                return String.format("Se ha saltado correctamente la canción: **%s**.", title);
            }
        };
    }
}
