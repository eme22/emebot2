package com.eme22.bolo.commands.dj;

import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.audio.RequestMetadata;
import com.eme22.bolo.commands.DJCommand;
import com.eme22.bolo.language.LanguageService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.io.IOException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class ForceskipCmd extends DJCommand {
   @ConfigProperty(name = "config.aliases.forceskip", defaultValue = "")
   String[] aliases = new String[0];

   public ForceskipCmd(Bot bot, @Named("djCategory") Category category) {
      super(bot, category);
      this.name = "forceskip";
      this.help = "skips the current song";
      this.bePlaying = true;
   }

   @Override
   public void doCommand(CommandEvent event) {
      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      
      handler.getAudioPlayer().ifPresentOrElse(player -> {
         RequestMetadata rm = null;
         try {
            rm = handler.getRequestMetadata();
         } catch (IOException var5) {
            event.replyError(lang.getMessage("command.music.next.error", event.getClient().getError()));
            return;
         }

         String addedBy = rm.owner() == 0L ? lang.getMessage("command.music.autoplay") : lang.getMessage("command.music.added.by", event.getJDA().getUserById(rm.user().id()).getAsMention());

         event.reply(lang.getMessage("command.music.skipped", event.getClient().getSuccess(), player.getTrack().getInfo().getTitle(), addedBy, event.getAuthor().getAsMention()));
         player.stopTrack().subscribe();
      }, () -> event.replyError(lang.getMessage("music.no.music.playing", "⏹", "")));
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      
      handler.getAudioPlayer().ifPresentOrElse(player -> {
         RequestMetadata rm = null;
         try {
            rm = handler.getRequestMetadata();
         } catch (IOException var5) {
            event.reply(lang.getMessage("command.music.next.error", event.getClient().getError())).setEphemeral(true).queue();
            return;
         }

         String addedBy = rm.owner() == 0L ? lang.getMessage("command.music.autoplay") : lang.getMessage("command.music.added.by", event.getJDA().getUserById(rm.user().id()).getAsMention());

         event.reply(lang.getMessage("command.music.skipped", event.getClient().getSuccess(), player.getTrack().getInfo().getTitle(), addedBy, event.getUser().getAsMention())).queue();
         player.stopTrack().subscribe();
      }, () -> event.reply(lang.getMessage("music.no.music.playing", "⏹", "")).setEphemeral(true).queue());
   }
}



