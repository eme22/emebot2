package com.eme22.bolo.commands.dj;

import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.commands.DJCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class PauseCmd extends DJCommand {
   @ConfigProperty(name = "config.aliases.pause", defaultValue = "")
   String[] aliases = new String[0];

   public PauseCmd(Bot bot, @Named("djCategory") Category category) {
      super(bot, category);
      this.name = "pause";
      this.help = "pauses the current song";
      this.bePlaying = true;
   }

   @Override
   public void doCommand(CommandEvent event) {
      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      if (handler.getAudioPlayer().get().getPaused()) {
         event.replyWarning("The player is already paused! Use `" + event.getClient().getPrefix() + "play` to unpause!");
      } else {
         handler.getAudioPlayer().get().setPaused(true);
         event.replySuccess(
            "Paused **" + handler.getAudioPlayer().get().getTrack().getInfo().getTitle() + "**. Type `" + event.getClient().getPrefix() + "play` to unpause!"
         );
      }
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      if (handler.getAudioPlayer().get().getPaused()) {
         event.reply(event.getClient().getWarning() + "The player is already paused! Use `" + event.getClient().getPrefix() + "play` to unpause!").queue();
      } else {
         handler.getAudioPlayer().get().setPaused(true);
         event.reply(
               event.getClient().getSuccess()
                  + "Paused **"
                  + handler.getAudioPlayer().get().getTrack().getInfo().getTitle()
                  + "**. Type `"
                  + event.getClient().getPrefix()
                  + "play` to unpause!"
            )
            .queue();
      }
   }
}



