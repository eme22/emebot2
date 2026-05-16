package com.eme22.bolo.commands.music;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.commands.MusicCommand;
import com.eme22.bolo.language.LanguageService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.Map;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class NowplayingCmd extends MusicCommand {
   @ConfigProperty(name = "config.aliases.nowplaying", defaultValue = "")
   String[] aliases = new String[0];

   public NowplayingCmd(Bot bot) {
      super(bot);
      this.name = "nowplaying";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "nowplaying",
         DiscordLocale.ENGLISH_US,
         "nowplaying",
         DiscordLocale.SPANISH,
         "reproduciendo",
         DiscordLocale.SPANISH_LATAM,
         "reproduciendo"
      );
      this.help = "Shows the song that is currently playing";
      this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
      this.descriptionLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "Shows the song that is currently playing",
         DiscordLocale.ENGLISH_US,
         "Shows the song that is currently playing",
         DiscordLocale.SPANISH,
         "Muestra la canciÃ³n que se estÃ¡ reproduciendo actualmente",
         DiscordLocale.SPANISH_LATAM,
         "Muestra la canciÃ³n que se estÃ¡ reproduciendo actualmente"
      );
   }

   @Override
   public void doCommand(CommandEvent event) {
      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      MessageCreateData m = handler.getNowPlaying(event.getJDA());
      if (m == null) {
         event.reply(handler.getNoMusicPlaying(event.getJDA(), lang));
         this.bot.getPlayerManager().getNowplayingHandler().clearLastNPMessage(event.getGuild());
      } else {
         event.reply(m, this.bot.getPlayerManager().getNowplayingHandler()::setLastNPMessage);
      }
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      MessageCreateData m = handler.getNowPlaying(event.getJDA());
      if (m == null) {
         event.reply(handler.getNoMusicPlaying(event.getJDA(), lang)).queue();
         this.bot.getPlayerManager().getNowplayingHandler().clearLastNPMessage(event.getGuild());
      } else {
         event.reply(m).queue(s -> s.retrieveOriginal().queue(this.bot.getPlayerManager().getNowplayingHandler()::setLastNPMessage));
      }
   }
}







