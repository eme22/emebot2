package com.eme22.bolo.commands.dj;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.audio.QueuedTrack;
import com.eme22.bolo.commands.DJCommand;
import com.eme22.bolo.queue.FairQueue;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class MoveTrackCmd extends DJCommand {
   @ConfigProperty(name = "config.aliases.movetrack", defaultValue = "")
   String[] aliases = new String[0];

   public MoveTrackCmd(Bot bot, @Named("djCategory") Category category) {
      super(bot, category);
      this.name = "movetrack";
      this.help = "move a track in the current queue to a different position";
      this.arguments = "<from> <to>";
      this.bePlaying = true;
   }

   @Override
   public void doCommand(CommandEvent event) {
      String[] parts = event.getArgs().split("\\s+", 2);
      if (parts.length < 2) {
         event.replyError("Please include two valid indexes.");
      } else {
         int from;
         int to;
         try {
            from = Integer.parseInt(parts[0]);
            to = Integer.parseInt(parts[1]);
         } catch (NumberFormatException var10) {
            event.replyError("Please provide two valid indexes.");
            return;
         }

         if (from == to) {
            event.replyError("Can't move a track to the same position.");
         } else {
            AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
            FairQueue<QueuedTrack> queue = handler.getQueueManager().getQueue();
            if (isUnavailablePosition(queue, from)) {
               String reply = String.format("`%d` is not a valid position in the queue!", from);
               event.replyError(reply);
            } else if (isUnavailablePosition(queue, to)) {
               String reply = String.format("`%d` is not a valid position in the queue!", to);
               event.replyError(reply);
            } else {
               QueuedTrack track = queue.moveItem(from - 1, to - 1);
               String trackTitle = track.getTrack().getInfo().getTitle();
               String reply = String.format("Moved **%s** from position `%d` to `%d`.", trackTitle, from, to);
               event.replySuccess(reply);
            }
         }
      }
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
   }

   private static boolean isUnavailablePosition(FairQueue<QueuedTrack> queue, int position) {
      return position < 1 || position > queue.size();
   }
}








