package com.eme22.bolo.commands.music;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.audio.RequestMetadata;
import com.eme22.bolo.commands.MusicCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.io.IOException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class SkipCmd extends MusicCommand {
   @ConfigProperty(name = "config.aliases.voteskip", defaultValue = "")
   String[] aliases = new String[0];

   public SkipCmd(Bot bot) {
      super(bot);
      this.name = "voteskip";
      this.help = "votes to skip the current song";
      this.beListening = true;
      this.bePlaying = true;
   }

   @Override
   public void doCommand(CommandEvent event) {
      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      RequestMetadata rm = null;

      try {
         rm = handler.getRequestMetadata();
      } catch (IOException var8) {
         event.replyError(" Error al obtener la informaciÃ³n de la canciÃ³n actual.");
         return;
      }

      if (event.getAuthor().getIdLong() == rm.owner()) {
         event.reply(event.getClient().getSuccess() + " Skipped **" + handler.getAudioPlayer().get().getTrack().getInfo().getTitle() + "**");
         handler.getAudioPlayer().get().stopTrack();
      } else {
         int listeners = (int)event.getSelfMember()
            .getVoiceState()
            .getChannel()
            .getMembers()
            .stream()
            .filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened())
            .count();
         String msg;
         if (handler.getVotes().contains(event.getAuthor().getId())) {
            msg = event.getClient().getWarning() + " Ya has votado para saltar esta cancion `[";
         } else {
            msg = event.getClient().getSuccess() + " Has votado para saltar esta cancion `[";
            handler.getVotes().add(event.getAuthor().getId());
         }

         int skippers = (int)event.getSelfMember()
            .getVoiceState()
            .getChannel()
            .getMembers()
            .stream()
            .filter(m -> handler.getVotes().contains(m.getUser().getId()))
            .count();
         int required = (int)Math.ceil(listeners * this.bot.getSettingsManager().getSettings(event.getGuild()).getSkipRatio());
         msg = msg + skippers + " votes, " + required + "/" + listeners + " needed]`";
         if (skippers >= required) {
            msg = msg
               + "\n"
               + event.getClient().getSuccess()
               + " Saltado **"
               + handler.getAudioPlayer().get().getTrack().getInfo().getTitle()
               + "** "
               + (rm.owner() == 0L ? "(autoplay)" : "(pedido por **" + rm.user().username() + "**)");
            handler.getAudioPlayer().get().stopTrack().subscribe();
         }

         event.reply(msg);
      }
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      RequestMetadata rm = null;

      try {
         rm = handler.getRequestMetadata();
      } catch (IOException var8) {
         event.reply(event.getClient().getError() + " Error al obtener la informaciÃ³n de la canciÃ³n actual.").setEphemeral(true).queue();
         return;
      }

      if (event.getUser().getIdLong() == rm.owner()) {
         event.reply(event.getClient().getSuccess() + " Saltado **" + handler.getAudioPlayer().get().getTrack().getInfo().getTitle() + "**").queue();
         handler.getAudioPlayer().get().stopTrack();
      } else {
         int listeners = (int)event.getGuild()
            .getSelfMember()
            .getVoiceState()
            .getChannel()
            .getMembers()
            .stream()
            .filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened())
            .count();
         String msg;
         if (handler.getVotes().contains(event.getUser().getId())) {
            msg = event.getClient().getWarning() + " Ya has votado para saltar esta cancion `[";
         } else {
            msg = event.getClient().getSuccess() + " Has votado para saltar esta cancion `[";
            handler.getVotes().add(event.getUser().getId());
         }

         int skippers = (int)event.getGuild()
            .getSelfMember()
            .getVoiceState()
            .getChannel()
            .getMembers()
            .stream()
            .filter(m -> handler.getVotes().contains(m.getUser().getId()))
            .count();
         int required = (int)Math.ceil(listeners * this.bot.getSettingsManager().getSettings(event.getGuild()).getSkipRatio());
         msg = msg + skippers + " votes, " + required + "/" + listeners + " needed]`";
         if (skippers >= required) {
            msg = msg
               + "\n"
               + event.getClient().getSuccess()
               + " Saltado **"
               + handler.getAudioPlayer().get().getTrack().getInfo().getTitle()
               + "** "
               + (rm.owner() == 0L ? "(autoplay)" : "(pedido por **" + rm.user().username() + "**)");
            handler.getAudioPlayer().get().stopTrack().subscribe();
         }

         event.reply(msg).queue();
      }
   }
}







