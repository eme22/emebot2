package com.eme22.bolo.commands.dj;

import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.commands.DJCommand;
import com.eme22.bolo.language.LanguageService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.Map;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class StopCmd extends DJCommand {
   @ConfigProperty(name = "config.aliases.stop", defaultValue = "")
   String[] aliases = new String[0];

   public StopCmd(Bot bot, @Named("djCategory") Category category) {
      super(bot, category);
      this.name = "stop";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_UK, "stop", DiscordLocale.ENGLISH_US, "stop", DiscordLocale.SPANISH, "detener", DiscordLocale.SPANISH_LATAM, "detener"
      );
      this.help = "stops the current song and clears the queue";
      this.bePlaying = false;
      this.descriptionLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "Stops the current song and clears the queue",
         DiscordLocale.ENGLISH_US,
         "Stops the current song and clears the queue",
         DiscordLocale.SPANISH,
         "Detiene la canciÃ³n actual y limpia la cola",
         DiscordLocale.SPANISH_LATAM,
         "Detiene la canciÃ³n actual y limpia la cola"
      );
   }

   @Override
   public void doCommand(CommandEvent event) {
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      handler.stopAndClear();
      event.getGuild().getAudioManager().closeAudioConnection();
      event.reply(lang.getMessage("command.stop.success", new Object[]{event.getClient().getSuccess()}));
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      handler.stopAndClear();
      event.getGuild().getAudioManager().closeAudioConnection();
      event.reply(lang.getMessage("command.stop.success", new Object[]{event.getClient().getSuccess()})).queue();
   }
}



