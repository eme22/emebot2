package com.eme22.bolo.ai.tools;

import com.eme22.bolo.Bot;
import com.eme22.bolo.ai.AITool;
import com.eme22.bolo.ai.OpenAIDTO;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.audio.RequestMetadata;
import com.eme22.bolo.audio.QueuedTrack;
import com.eme22.bolo.audio.AudioLoadResultHandler;
import com.eme22.bolo.model.RepeatMode;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.language.LanguageService;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

    @Produces
    @ApplicationScoped
    public AITool getPlayMusicTool() {
        return new AITool() {
            @Override
            public String getName() {
                return "play_music";
            }

            @Override
            public String getDescription() {
                return "Busca y reproduce una canción o lista de reproducción en el canal de voz actual del usuario.";
            }

            @Override
            public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> queryProp = new HashMap<>();
                queryProp.put("type", "string");
                queryProp.put("description", "El nombre de la canción, artista, enlace de YouTube/Spotify, o búsqueda a reproducir.");

                Map<String, Object> properties = new HashMap<>();
                properties.put("query", queryProp);

                List<String> required = new ArrayList<>();
                required.add("query");

                return OpenAIDTO.Tool.builder()
                        .type("function")
                        .function(OpenAIDTO.FunctionDefinition.builder()
                                .name(getName())
                                .description(getDescription())
                                .parameters(OpenAIDTO.ParametersDefinition.builder()
                                        .type("object")
                                        .properties(properties)
                                        .required(required)
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
                String query = (String) arguments.get("query");
                if (query == null || query.trim().isEmpty()) {
                    return "Error: Debes proporcionar un término de búsqueda o enlace en el argumento 'query'.";
                }

                Server settings = bot.getSettingsManager().getSettings(event.getGuild());
                int voiceCheck = com.eme22.bolo.utils.OtherUtil.isUserInVoice(event.getGuild(), settings, event.getMember());
                if (voiceCheck == 0) {
                    return "Error: Debes estar conectado a un canal de voz para poder reproducir música.";
                } else if (voiceCheck == 2) {
                    return "Error: No puedes reproducir música en el canal AFK.";
                }

                if (!com.eme22.bolo.utils.OtherUtil.isAudioChannelAllowed(event.getGuild(), settings, event.getMember())) {
                    net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel configuredChannel = event.getGuild().getVoiceChannelById(settings.getVoiceChannelId());
                    if (configuredChannel != null) {
                        return String.format("Error: Debes estar conectado al canal de voz de música configurado: %s.", configuredChannel.getName());
                    } else {
                        return "Error: Debes estar conectado al mismo canal de voz que el bot para reproducir música.";
                    }
                }

                net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion userChannel = event.getMember().getVoiceState().getChannel();
                
                try {
                    bot.getPlayerManager().setUpHandler(event.getGuild(), userChannel.asVoiceChannel());
                    event.getGuild().getJDA().getDirectAudioController().connect(userChannel);
                } catch (Exception e) {
                    return "Error al intentar conectar al canal de voz: " + e.getMessage();
                }

                String identifier = query.trim();
                if (!identifier.startsWith("http://") && !identifier.startsWith("https://")) {
                    identifier = "ytsearch:" + identifier;
                }

                CompletableFuture<String> future = new CompletableFuture<>();
                AudioLoadResultHandler resultHandler = new AudioLoadResultHandler() {
                    private final dev.arbjerg.lavalink.client.FunctionalLoadResultHandler realHandler = new dev.arbjerg.lavalink.client.FunctionalLoadResultHandler(
                        trackLoaded -> {
                            Track track = trackLoaded.getTrack();
                            if (bot.getPlayerManager().isTooLong(track)) {
                                future.complete("Error: La canción es demasiado larga (límite de 1 hora).");
                                return;
                            }
                            AudioHandler ah = bot.getPlayerManager().getAudioHandler(event.getGuild());
                            int pos = ah.getQueueManager().addToTrackQueue(new QueuedTrack(track, event.getAuthor(), event.getGuild())) + 1;
                            future.complete(String.format("Éxito: Se ha añadido a la cola y se reproducirá: **%s** (%s). Posición en cola: %d.", 
                                track.getInfo().getTitle(), 
                                com.eme22.bolo.utils.FormatUtil.formatTime(track.getInfo().getLength()), 
                                pos));
                        },
                        playlist -> {
                            if (playlist.getTracks().isEmpty()) {
                                future.complete("Error: La lista de reproducción está vacía.");
                                return;
                            }
                            AudioHandler ah = bot.getPlayerManager().getAudioHandler(event.getGuild());
                            if (playlist.getTracks().size() == 1) {
                                Track track = playlist.getTracks().get(0);
                                int pos = ah.getQueueManager().addToTrackQueue(new QueuedTrack(track, event.getAuthor(), event.getGuild())) + 1;
                                future.complete(String.format("Éxito: Se ha añadido a la cola: **%s** (%s). Posición: %d.", 
                                    track.getInfo().getTitle(), 
                                    com.eme22.bolo.utils.FormatUtil.formatTime(track.getInfo().getLength()), 
                                    pos));
                            } else {
                                int count = 0;
                                for (Track track : playlist.getTracks()) {
                                    if (!bot.getPlayerManager().isTooLong(track)) {
                                        ah.getQueueManager().addToTrackQueue(new QueuedTrack(track, event.getAuthor(), event.getGuild()));
                                        count++;
                                    }
                                }
                                future.complete(String.format("Éxito: Se han añadido **%d** canciones de la lista de reproducción: **%s**.", 
                                    count, playlist.getInfo().getName()));
                            }
                        },
                        searchResult -> {
                            if (searchResult.getTracks().isEmpty()) {
                                future.complete("Error: No se encontraron coincidencias para la búsqueda.");
                                return;
                            }
                            Track track = (Track) searchResult.getTracks().get(0);
                            if (bot.getPlayerManager().isTooLong(track)) {
                                future.complete("Error: La canción es demasiado larga (límite de 1 hora).");
                                return;
                            }
                            AudioHandler ah = bot.getPlayerManager().getAudioHandler(event.getGuild());
                            int pos = ah.getQueueManager().addToTrackQueue(new QueuedTrack(track, event.getAuthor(), event.getGuild())) + 1;
                            future.complete(String.format("Éxito: Se ha añadido a la cola: **%s** (%s) en la posición %d.", 
                                track.getInfo().getTitle(), 
                                com.eme22.bolo.utils.FormatUtil.formatTime(track.getInfo().getLength()), 
                                pos));
                        },
                        () -> future.complete("Error: No se encontraron coincidencias para la búsqueda."),
                        loadFailed -> future.complete("Error al cargar la pista: " + loadFailed.getException().getMessage())
                    );

                    @Override
                    public void trackLoaded(Track track) {}

                    @Override
                    public void playlistLoaded(PlaylistLoaded playlist) {}

                    @Override
                    public void noMatches() {}

                    @Override
                    public void loadFailed(dev.arbjerg.lavalink.client.player.LoadFailed throwable) {}

                    @Override
                    public void loadFailed(String error) {
                        future.complete("Error de carga: " + error);
                    }

                    @Override
                    public dev.arbjerg.lavalink.client.FunctionalLoadResultHandler getRealResultHandler() {
                        return realHandler;
                    }
                };

                bot.getPlayerManager().loadItem(event.getGuild().getIdLong(), identifier, resultHandler);
                
                try {
                    return future.get(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    return "La solicitud de reproducción se está procesando en segundo plano, o tardó demasiado en responder.";
                }
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getGetQueueTool() {
        return new AITool() {
            @Override
            public String getName() {
                return "get_queue";
            }

            @Override
            public String getDescription() {
                return "Obtiene la lista actual de canciones en la cola de reproducción del servidor.";
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
                if (handler == null) {
                    return "No se ha inicializado el reproductor de música en este servidor.";
                }

                List<QueuedTrack> list = handler.getQueueManager().getQueue().getList();
                if (list.isEmpty()) {
                    if (handler.isMusicPlaying(event.getJDA())) {
                        String title = handler.getAudioPlayer().get().getTrack().getInfo().getTitle();
                        return String.format("La cola está vacía. Actualmente se está reproduciendo únicamente: **%s**.", title);
                    }
                    return "No hay ninguna canción en la cola de reproducción ni reproduciéndose.";
                }

                StringBuilder sb = new StringBuilder();
                if (handler.isMusicPlaying(event.getJDA())) {
                    String title = handler.getAudioPlayer().get().getTrack().getInfo().getTitle();
                    sb.append("🎶 **Reproduciendo ahora:** ").append(title).append("\n\n");
                }
                
                sb.append("📋 **Cola de reproducción (primeras 15 canciones):**\n");
                int limit = Math.min(list.size(), 15);
                long totalDurationMs = 0;
                
                for (int i = 0; i < list.size(); i++) {
                    QueuedTrack qt = list.get(i);
                    Track track = qt.getTrack();
                    totalDurationMs += track.getInfo().getLength();
                    if (i < limit) {
                        String requestedBy = "Desconocido";
                        try {
                            RequestMetadata rm = track.getUserData(RequestMetadata.class);
                            if (rm != null) {
                                requestedBy = rm.owner() == 0L ? "Autoplay" : rm.user().username();
                            }
                        } catch (Exception e) {
                            // ignore
                        }
                        
                        sb.append(String.format("%d. [%s] **%s** - por %s\n", 
                            i + 1, 
                            com.eme22.bolo.utils.FormatUtil.formatTime(track.getInfo().getLength()), 
                            track.getInfo().getTitle(),
                            requestedBy
                        ));
                    }
                }

                if (list.size() > limit) {
                    sb.append(String.format("... y %d canciones más.\n", list.size() - limit));
                }

                sb.append(String.format("\nTotal de canciones en cola: %d | Duración total: `%s`", 
                    list.size(), 
                    com.eme22.bolo.utils.FormatUtil.formatTime(totalDurationMs)));

                return sb.toString();
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getStopMusicTool() {
        return new AITool() {
            @Override
            public String getName() {
                return "stop_music";
            }

            @Override
            public String getDescription() {
                return "Detiene la reproducción de música por completo, limpia la cola y desconecta al bot del canal de voz.";
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
                if (handler == null) {
                    return "No hay reproductor activo para detener.";
                }

                handler.stopAndClear();
                event.getGuild().getAudioManager().closeAudioConnection();
                return "Se ha detenido la música, limpiado la cola y me he desconectado del canal de voz.";
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getPauseMusicTool() {
        return new AITool() {
            @Override
            public String getName() {
                return "pause_music";
            }

            @Override
            public String getDescription() {
                return "Pausa la reproducción de la canción actual.";
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
                    return "No hay ninguna canción reproduciéndose en este momento.";
                }

                if (handler.getAudioPlayer().get().getPaused()) {
                    return "El reproductor de música ya está pausado.";
                }

                handler.getAudioPlayer().get().setPaused(true);
                return "Se ha pausado la reproducción de la canción actual.";
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getResumeMusicTool() {
        return new AITool() {
            @Override
            public String getName() {
                return "resume_music";
            }

            @Override
            public String getDescription() {
                return "Reanuda la reproducción de la música si estaba pausada.";
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
                if (handler == null || handler.getAudioPlayer().get().getTrack() == null) {
                    return "No hay ninguna canción cargada para reanudar.";
                }

                if (!handler.getAudioPlayer().get().getPaused()) {
                    return "El reproductor de música ya se está reproduciendo.";
                }

                handler.getAudioPlayer().get().setPaused(false);
                return "Se ha reanudado la reproducción de la música.";
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getShuffleQueueTool() {
        return new AITool() {
            @Override
            public String getName() {
                return "shuffle_queue";
            }

            @Override
            public String getDescription() {
                return "Mezcla aleatoriamente las canciones de la cola de reproducción.";
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
                if (handler == null || handler.getQueueManager().getQueue().getList().isEmpty()) {
                    return "La cola de reproducción está vacía, no hay nada que mezclar.";
                }

                handler.getQueueManager().shuffle();
                return "Se ha mezclado la cola de reproducción correctamente.";
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getSetRepeatModeTool() {
        return new AITool() {
            @Override
            public String getName() {
                return "set_repeat_mode";
            }

            @Override
            public String getDescription() {
                return "Configura el modo de repetición (desactivado, repetir todo, o repetir una sola canción).";
            }

            @Override
            public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> modeProp = new HashMap<>();
                modeProp.put("type", "string");
                modeProp.put("description", "El modo de repetición: 'off' (desactivado), 'all' (repetir toda la cola), o 'single' (repetir la canción actual).");
                List<String> enums = new ArrayList<>();
                enums.add("off");
                enums.add("all");
                enums.add("single");
                modeProp.put("enum", enums);

                Map<String, Object> properties = new HashMap<>();
                properties.put("mode", modeProp);

                List<String> required = new ArrayList<>();
                required.add("mode");

                return OpenAIDTO.Tool.builder()
                        .type("function")
                        .function(OpenAIDTO.FunctionDefinition.builder()
                                .name(getName())
                                .description(getDescription())
                                .parameters(OpenAIDTO.ParametersDefinition.builder()
                                        .type("object")
                                        .properties(properties)
                                        .required(required)
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
                String modeStr = (String) arguments.get("mode");
                if (modeStr == null || modeStr.trim().isEmpty()) {
                    return "Error: Debes proporcionar un modo en el argumento 'mode' ('off', 'all' o 'single').";
                }

                RepeatMode value;
                try {
                    value = RepeatMode.of(modeStr.trim().toLowerCase());
                } catch (IllegalArgumentException e) {
                    return "Error: Modo de repetición inválido. Los modos válidos son: 'off', 'all' o 'single'.";
                }

                Server settings = bot.getSettingsManager().getSettings(event.getGuild());
                settings.setRepeatMode(value);
                bot.getSettingsManager().saveSettings(settings);

                LanguageService lang = bot.getSettingsManager().getLanguageService(event.getGuild());
                return "El modo de repetición ahora está configurado en: `" + lang.getMessage("music.repeat." + value.getKey()) + "`";
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getSetMusicEffectTool() {
        return new AITool() {
            @Override
            public String getName() {
                return "set_music_effect";
            }

            @Override
            public String getDescription() {
                return "Aplica un efecto de audio/filtro a la música en reproducción en el servidor (como bassboost, nightcore, vaporwave, karaoke, distortion, o ninguno para limpiar).";
            }

            @Override
            public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> effectProp = new HashMap<>();
                effectProp.put("type", "string");
                effectProp.put("description", "El efecto de música a aplicar: 'bassboost', 'nightcore', 'vaporwave', 'karaoke', 'distortion', o 'none'.");
                List<String> enums = Arrays.asList("bassboost", "nightcore", "vaporwave", "karaoke", "distortion", "none");
                effectProp.put("enum", enums);

                Map<String, Object> properties = new HashMap<>();
                properties.put("effect", effectProp);

                List<String> required = Collections.singletonList("effect");

                return OpenAIDTO.Tool.builder()
                        .type("function")
                        .function(OpenAIDTO.FunctionDefinition.builder()
                                .name(getName())
                                .description(getDescription())
                                .parameters(OpenAIDTO.ParametersDefinition.builder()
                                        .type("object")
                                        .properties(properties)
                                        .required(required)
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
                String effectStr = (String) arguments.get("effect");
                if (effectStr == null || effectStr.trim().isEmpty()) {
                    return "Error: Debes proporcionar un efecto en el argumento 'effect'.";
                }

                AudioHandler handler = bot.getPlayerManager().getAudioHandler(event.getGuild());
                if (handler == null || !handler.isMusicPlaying(event.getJDA())) {
                    return "No se está reproduciendo música en este servidor en este momento para poder aplicar un efecto.";
                }

                String cleanEffect = effectStr.trim().toLowerCase();
                if (!cleanEffect.startsWith("effect_")) {
                    cleanEffect = "effect_" + cleanEffect;
                }

                List<String> validEffects = Arrays.asList("effect_bassboost", "effect_nightcore", "effect_vaporwave", "effect_karaoke", "effect_distortion", "effect_none");
                if (!validEffects.contains(cleanEffect)) {
                    return "Error: Efecto de música inválido. Los efectos válidos son: 'bassboost', 'nightcore', 'vaporwave', 'karaoke', 'distortion', o 'none'.";
                }

                handler.setEffect(cleanEffect);
                LanguageService lang = bot.getSettingsManager().getLanguageService(event.getGuild());
                String translatedEffectName = lang.getMessage(cleanEffect.replace("_", ".") + ".label");
                return String.format("Se ha aplicado correctamente el efecto musical: **%s**.", translatedEffectName);
            }
        };
    }

    @Produces
    @ApplicationScoped
    public AITool getSearchLyricsTool() {
        return new AITool() {
            @Override
            public String getName() {
                return "search_lyrics";
            }

            @Override
            public String getDescription() {
                return "Busca las letras de una canción específica (o de la canción que suena en este momento si no se proporciona el argumento query).";
            }

            @Override
            public OpenAIDTO.Tool getDefinition() {
                Map<String, Object> queryProp = new HashMap<>();
                queryProp.put("type", "string");
                queryProp.put("description", "El título de la canción a buscar (opcional. Si se omite, buscará la letra de la canción que suena en reproducción en este momento).");

                Map<String, Object> properties = new HashMap<>();
                properties.put("query", queryProp);

                return OpenAIDTO.Tool.builder()
                        .type("function")
                        .function(OpenAIDTO.FunctionDefinition.builder()
                                .name(getName())
                                .description(getDescription())
                                .parameters(OpenAIDTO.ParametersDefinition.builder()
                                        .type("object")
                                        .properties(properties)
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
                String query = (String) arguments.get("query");
                String title = null;

                if (query != null && !query.trim().isEmpty()) {
                    title = query.trim();
                } else {
                    AudioHandler handler = bot.getPlayerManager().getAudioHandler(event.getGuild());
                    if (handler != null && handler.isMusicPlaying(event.getJDA())) {
                        title = handler.getAudioPlayer().get().getTrack().getInfo().getTitle();
                    }
                }

                if (title == null || title.isEmpty()) {
                    return "Error: No se proporcionó un título de canción y no hay música en reproducción para obtener las letras.";
                }

                com.jagrosh.jlyrics.Lyrics lyrics = com.eme22.bolo.utils.OtherUtil.getLyrics(title);
                if (lyrics == null) {
                    return String.format("No se encontraron letras para la canción: '%s'.", title);
                }

                String content = lyrics.getContent();
                if (content.length() > 3000) {
                    content = content.substring(0, 3000) + "\n\n...(Letra demasiado larga, truncada)...";
                }

                return String.format("Letras encontradas para **%s** por **%s**:\n\n%s", 
                    lyrics.getTitle(), lyrics.getAuthor(), content);
            }
        };
    }
}
