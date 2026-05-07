package com.eme22.bolo.commands.owner;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.OwnerCommand;
import com.eme22.bolo.configuration.LavalinkProperties;
import com.eme22.bolo.language.LanguageService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.NodeOptions.Builder;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class ReloadNodesCmd extends OwnerCommand {
   private final Bot bot;
   private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
   @ConfigProperty(name = "config.aliases.reloadnodes", defaultValue = "")
   String[] aliases = new String[0];
   @Inject
   private LavalinkProperties properties;
   
   @Inject
   private LavalinkClient lavalink;

   public ReloadNodesCmd(@NotNull Bot bot) {
      this.bot = bot;
      this.name = "reloadnodes";
      this.help = "reloads the lavalink nodes";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "reloadnodes",
         DiscordLocale.ENGLISH_US,
         "reloadnodes",
         DiscordLocale.PORTUGUESE_BRAZILIAN,
         "recarregar-nodes",
         DiscordLocale.SPANISH,
         "recargar-nodos",
         DiscordLocale.SPANISH_LATAM,
         "recargar-nodos"
      );
      this.descriptionLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "reloads the lavalink nodes",
         DiscordLocale.ENGLISH_US,
         "reloads the lavalink nodes",
         DiscordLocale.PORTUGUESE_BRAZILIAN,
         "recarrega os nodes do lavalink",
         DiscordLocale.SPANISH,
         "recarga los nodos de lavalink",
         DiscordLocale.SPANISH_LATAM,
         "recarga los nodos de lavalink"
      );
      this.guildOnly = false;
   }

   protected void execute(SlashCommandEvent event) {
      this.fireRefreshEvent();
      this.registerLavalinkNodes();
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      event.reply(languageService.getMessage("reloadnodes.success")).queue();
   }

   protected void execute(CommandEvent event) {
      this.fireRefreshEvent();
      this.removeNodes();
      this.registerLavalinkNodes();
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      event.reply(languageService.getMessage("reloadnodes.success"));
   }

   private void removeNodes() {
      this.lavalink.getNodes().forEach(node -> this.lavalink.removeNode(node));
   }

   private void fireRefreshEvent() {
      // this.eventPublisher.publishEvent(new RefreshEvent(this, "RefreshEvent", "Refreshing scope"));
   }

   private void registerLavalinkNodes() {
      this.properties
         .servers()
         .forEach(
            s -> {
               Builder options = new Builder()
                  .setName(s.name())
                  .setPassword(s.password())
                  .setServerUri(URI.create("ws://" + s.host() + ":" + s.port() + "/"))
                  .setHttpTimeout(s.timeout());
               this.lavalink.addNode(options.build());
            }
         );
   }
}



