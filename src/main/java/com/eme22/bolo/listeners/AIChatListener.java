package com.eme22.bolo.listeners;

import com.eme22.bolo.Bot;
import com.eme22.bolo.ai.AIChatService;
import com.eme22.bolo.ai.AIChatRateLimiter;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.services.UserOffenseService;
import com.eme22.bolo.model.UserOffense;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@Slf4j
public class AIChatListener implements EventListener {

    private static final long TYPING_INTERVAL_SECONDS = 8;

    @Inject
    Bot bot;

    @Inject
    AIChatService chatService;

    @Inject
    AIChatRateLimiter rateLimiter;

    @Inject
    UserOffenseService offenseService;

    private ScheduledExecutorService typingScheduler;

    @PostConstruct
    void initScheduler() {
        typingScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ai-typing-indicator");
            t.setDaemon(true);
            return t;
        });
    }

    @PreDestroy
    void shutdownScheduler() {
        if (typingScheduler != null) {
            typingScheduler.shutdownNow();
        }
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof MessageReceivedEvent messageReceivedEvent) {
            onMessageReceived(messageReceivedEvent);
        }
    }

    @ActivateRequestContext
    public void onMessageReceived(MessageReceivedEvent event) {
        // 1. Ignore bot messages
        if (event.getAuthor().isBot()) {
            return;
        }

        // 2. Ignore messages outside guilds
        if (!event.isFromGuild()) {
            return;
        }

        // 3. Retrieve Server settings
        Server server = bot.getSettingsManager().getSettings(event.getGuild());
        if (server == null || !server.isAiEnabled()) {
            return; // AI is not enabled for this server
        }

        long aiChannelId = server.getAiChannelId();
        boolean isExclusiveChannel = aiChannelId != 0L && event.getChannel().getIdLong() == aiChannelId;
        boolean isMentioned = event.getMessage().getMentions().isMentioned(event.getJDA().getSelfUser());
        boolean isReplyToBot = false;
        if (event.getMessage().getReferencedMessage() != null) {
            isReplyToBot = event.getMessage().getReferencedMessage().getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong();
        }

        boolean interactionTriggered = isMentioned || isReplyToBot;

        String rawMessage = event.getMessage().getContentRaw();
        String cleanMessage = rawMessage.replaceAll("<@!?" + event.getJDA().getSelfUser().getId() + ">", "").trim();
        boolean shouldRespond = false;
        if (interactionTriggered) {
            if (isExclusiveChannel) {
                shouldRespond = true;
            } else if (!server.isAiExclusive()) {
                shouldRespond = true;
            } else {
                long musicChannelId = server.getTextChannelId();
                boolean isMusicChannel = musicChannelId != 0L && event.getChannel().getIdLong() == musicChannelId;
                if (isMusicChannel && isSongRequestOrSearch(cleanMessage)) {
                    shouldRespond = true;
                }
            }
        }

        if (shouldRespond) {
            long userId = event.getAuthor().getIdLong();
            if (offenseService.isBanned(userId)) {
                UserOffense offense = offenseService.getOrCreateOffenses(userId);
                long epochSec = offense.getBanUntil().getEpochSecond();
                String banMsg = String.format("🚫 **Sistema de Seguridad**: Has sido bloqueado temporalmente para conversar conmigo debido a reiteradas conductas inapropiadas o violaciones de seguridad (%d ofensas registradas). Tu bloqueo expira el <t:%d:F> (<t:%d:R>).", 
                    offense.getOffenseCount(), epochSec, epochSec);
                event.getMessage().reply(banMsg).queue();
                return;
            }

            if (cleanMessage.isEmpty() && isMentioned) {
                event.getMessage().reply("¡Hola! ¿En qué puedo ayudarte hoy? Escríbeme o pregúntame algo.").queue();
                return;
            }

            // Rate limiting: cooldown por usuario para proteger la cuota de proveedores
            long requestUserId = event.getAuthor().getIdLong();
            if (rateLimiter.isUserInCooldown(requestUserId)) {
                log.debug("[AI Listener] Mensaje de usuario {} ignorado por cooldown anti-spam.", requestUserId);
                return;
            }

            // Límite de solicitudes concurrentes por canal
            long requestChannelId = event.getChannel().getIdLong();
            if (!rateLimiter.tryAcquireChannel(requestChannelId)) {
                event.getMessage().reply("⏳ Estoy procesando otra consulta en este canal. Espera un momento y vuelve a intentarlo.").queue();
                return;
            }

            log.info("[AI Listener] Mensaje de chat recibido de usuario '{}' (ID={}) en canal '{}' (ID={}) del servidor '{}'", 
                     event.getAuthor().getName(), event.getAuthor().getIdLong(), 
                     event.getChannel().getName(), event.getChannel().getIdLong(), 
                     event.getGuild().getName());

            // Indicador de escritura recurrente (el estado 'typing' de Discord dura ~10s y los loops de herramientas pueden tardar más)
            ScheduledFuture<?> typingTask = typingScheduler.scheduleAtFixedRate(
                    () -> {
                        try {
                            event.getChannel().sendTyping().queue();
                        } catch (Exception ignored) {
                        }
                    },
                    0, TYPING_INTERVAL_SECONDS, TimeUnit.SECONDS);

            // Execute asynchronously using the bot's thread pool to not block JDA thread
            bot.getThreadpool().submit(() -> {
                long listenerStartTime = System.currentTimeMillis();
                try {
                    AIChatService.AIChatResult result = chatService.processChatMessage(event, cleanMessage);
                    long duration = System.currentTimeMillis() - listenerStartTime;
                    log.info("[AI Listener] Procesamiento de IA completado en {} ms.", duration);

                    if (result != null && result.getContent() != null && !result.getContent().trim().isEmpty()) {
                        String content = result.getContent().trim();
                        Long dbMessageId = result.getDbMessageId();
                        boolean isFirst = true;
                        while (content.length() > 2000) {
                            int index = content.lastIndexOf("\n\n", 2000);
                            if (index <= 0) {
                                index = content.lastIndexOf("\n", 2000);
                            }
                            if (index <= 0) {
                                index = content.lastIndexOf(" ", 2000);
                            }
                            if (index <= 0) {
                                index = 2000;
                            }
                            
                            String chunk = content.substring(0, index).trim();
                            if (!chunk.isEmpty()) {
                                if (isFirst) {
                                    net.dv8tion.jda.api.entities.Message sentMsg = event.getMessage().reply(chunk).complete();
                                    if (dbMessageId != null) {
                                        chatService.updateDiscordMessageId(dbMessageId, sentMsg.getIdLong());
                                    }
                                    isFirst = false;
                                } else {
                                    event.getChannel().sendMessage(chunk).complete();
                                }
                            }
                            content = content.substring(index).trim();
                        }
                        
                        if (!content.isEmpty()) {
                            if (isFirst) {
                                event.getMessage().reply(content).queue(sentMsg -> {
                                    if (dbMessageId != null) {
                                        chatService.updateDiscordMessageId(dbMessageId, sentMsg.getIdLong());
                                    }
                                });
                            } else {
                                event.getChannel().sendMessage(content).queue();
                            }
                        }
                    } else {
                        log.warn("[AI Listener] El resultado del procesamiento de IA fue nulo o vacío.");
                    }
                } catch (Exception e) {
                    long duration = System.currentTimeMillis() - listenerStartTime;
                    log.error("[AI Listener] Error procesando respuesta de IA de manera asíncrona tras {} ms", duration, e);
                    event.getMessage().reply(chatService.getFriendlyErrorMessage(e)).queue();
                } finally {
                    typingTask.cancel(false);
                    rateLimiter.releaseChannel(requestChannelId);
                }
            });
        }
    }

    private boolean isSongRequestOrSearch(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        String lower = message.toLowerCase();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?U)\\b(reproduc|reprodúc|busc|búsc|pon|cancion|canción|music|músic|play|song|cant|cánt|tema|temazo|search|find)"
        );
        return pattern.matcher(lower).find();
    }
}

