package com.eme22.bolo.commands.music;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.commands.MusicCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class ShuffleCmd extends MusicCommand {
   @ConfigProperty(name = "config.aliases.shuffle", defaultValue = "")
   String[] aliases = new String[0];

   public ShuffleCmd(Bot bot) {
      super(bot);
      this.name = "shuffle";
      this.help = "shuffles songs you have added";
      this.beListening = true;
      this.bePlaying = true;
   }

   @Override
   public void doCommand(CommandEvent event) {
      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      int s = handler.getQueueManager().getQueue().shuffleMy(event.getAuthor().getIdLong());
      switch (s) {
         case 0:
            event.replyError("You don't have any music in the queue to shuffle!");
            break;
         case 1:
            event.replyWarning("You only have one song in the queue!");
            break;
         default:
            event.replySuccess("You successfully shuffled your " + s + " entries.");
      }
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      int s = handler.getQueueManager().getQueue().shuffleMy(event.getUser().getIdLong());
      switch (s) {
         case 0:
            event.reply(event.getClient().getError() + "You don't have any music in the queue to shuffle!").queue();
            break;
         case 1:
            event.reply(event.getClient().getWarning() + "You only have one song in the queue!").queue();
            break;
         default:
            event.reply(event.getClient().getSuccess() + "You successfully shuffled your " + s + " entries.").queue();
      }
   }
}


