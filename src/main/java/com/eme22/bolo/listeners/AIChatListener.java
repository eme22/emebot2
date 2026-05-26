package com.eme22.bolo.listeners;

import com.eme22.bolo.Bot;
import com.eme22.bolo.ai.AIChatService;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.services.UserOffenseService;
import com.eme22.bolo.model.UserOffense;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
@Slf4j
public class AIChatListener implements EventListener {

    @Inject
    Bot bot;

    @Inject
    AIChatService chatService;

    @Inject
    UserOffenseService offenseService;

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

        // 4. Trigger AI if exclusive channel is used OR (if NOT exclusive-only) the bot is mentioned OR it is a reply to the bot
        boolean shouldRespond = false;
        if (isExclusiveChannel) {
            shouldRespond = true;
        } else if (!server.isAiExclusive()) {
            shouldRespond = isMentioned || isReplyToBot;
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

            String rawMessage = event.getMessage().getContentRaw();

            // Clean up the mention from the prompt
            String cleanMessage = rawMessage.replaceAll("<@!?" + event.getJDA().getSelfUser().getId() + ">", "").trim();
            if (cleanMessage.isEmpty() && isMentioned) {
                event.getMessage().reply("¡Hola! ¿En qué puedo ayudarte hoy? Escríbeme o pregúntame algo.").queue();
                return;
            }

            // Send typing indicator to channel
            event.getChannel().sendTyping().queue();

            // Execute asynchronously using the bot's thread pool to not block JDA thread
            bot.getThreadpool().submit(() -> {
                try {
                    AIChatService.AIChatResult result = chatService.processChatMessage(event, cleanMessage);
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
                    }
                } catch (Exception e) {
                    log.error("Error processing AI response asynchronously", e);
                    event.getMessage().reply(chatService.getFriendlyErrorMessage(e)).queue();
                }
            });
        }
    }
}
