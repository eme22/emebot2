package com.eme22.bolo.listeners;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.Bot;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.model.LinkEnhancer;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.stats.StatsService;
import java.util.List;
import java.util.regex.Pattern;
import lombok.Generated;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.context.control.ActivateRequestContext;



@ApplicationScoped
@Slf4j
public class LinkEnhancerListener extends ListenerAdapter {
   
   private final Bot bot;
   @ConfigProperty(name = "config.update")
   boolean updatealerts;
   @ConfigProperty(name = "config.owner")
   long owner;
   @ConfigProperty(name = "config.clientToken", defaultValue = "")
   String clientToken;
   
   @ConfigProperty(name = "quarkus.application.version")
   String version;
   
   private final StatsService statsService;

   @Inject
   public LinkEnhancerListener(Bot bot, StatsService statsService) {
      this.bot = bot;
      this.statsService = statsService;
   }

   @ActivateRequestContext
   @Transactional
   public void onMessageReceived(@NotNull MessageReceivedEvent event) {
      if (!event.getAuthor().isBot()) {
         if (event.isFromGuild()) {
            if (event.getGuild().getIdLong() != 0L) {
               Server server = this.bot.getSettingsManager().getSettings(event.getGuild());
               if (server.getLinkEnhancerEnabled() && !server.getLinkEnhancers().isEmpty()) {
                  if (server.getLinkEnhancerChannels().contains(event.getChannel().getIdLong())) {
                     String message = event.getMessage().getContentRaw();
                     List<LinkEnhancer> enhancers = server.getLinkEnhancers();
                     LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());

                     for (LinkEnhancer enhancer : enhancers) {
                        Pattern linkPattern = Pattern.compile(enhancer.getLinkEnhancerLinkRegex());
                        if (linkPattern.matcher(message).find()) {
                           Pattern replacementPattern = Pattern.compile(enhancer.getLinkEnhancerEnhancerRegex());
                           message = message.replaceAll(replacementPattern.pattern(), enhancer.getLinkEnhancerReplacement());
                           event.getMessage().delete().queue();
                           event.getChannel()
                              .sendMessage(languageService.getMessage("linkenhancer.message", new Object[]{event.getAuthor().getAsMention(), message}))
                              .queue();
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
