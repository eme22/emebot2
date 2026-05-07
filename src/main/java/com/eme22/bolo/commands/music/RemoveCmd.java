package com.eme22.bolo.commands.music;

import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.audio.QueuedTrack;
import com.eme22.bolo.commands.MusicCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.Collections;
import lombok.Generated;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Slf4j
public class RemoveCmd extends MusicCommand {
   @Generated
   
   @ConfigProperty(name = "config.aliases.remove", defaultValue = "")
   String[] aliases = new String[0];

   public RemoveCmd(Bot bot) {
      super(bot);
      this.name = "remove";
      this.help = "removes a song from the queue";
      this.arguments = "<position|ALL>";
      this.beListening = true;
      this.bePlaying = true;
      this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "posicion", "Posicion para eliminar de la cola").setRequired(true));
   }

   @Override
   public void doCommand(CommandEvent event) {
      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      if (handler.getQueueManager().getQueue().isEmpty()) {
         event.replyError("There is nothing in the queue!");
      } else if (event.getArgs().equalsIgnoreCase("all")) {
         int count = handler.getQueueManager().getQueue().removeAll(String.valueOf(event.getAuthor().getIdLong()));
         if (count == 0) {
            event.replyWarning("You don't have any songs in the queue!");
         } else {
            event.replySuccess("Successfully removed your " + count + " entries.");
         }
      } else {
         int pos;
         try {
            pos = Integer.parseInt(event.getArgs());
         } catch (NumberFormatException var10) {
            pos = 0;
         }

         if (pos >= 1 && pos <= handler.getQueueManager().getQueue().size()) {
            Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
            boolean isDJ = event.getMember().hasPermission(new Permission[]{Permission.MANAGE_SERVER});
            if (!isDJ) {
               isDJ = event.getMember().getRoles().contains(event.getGuild().getRoleById(settings.getDjRoleId()));
            }

            QueuedTrack qt = handler.getQueueManager().getQueue().get(pos - 1);
            log.debug("Comparing {} to {}", qt.getUserData(), event.getAuthor().getIdLong());
            if (qt.getUserData().equals(event.getAuthor().getIdLong())) {
               handler.getQueueManager().getQueue().remove(pos - 1);
               event.replySuccess("Removed **" + qt.getTrack().getInfo().getTitle() + "** from the queue");
            } else if (isDJ) {
               handler.getQueueManager().getQueue().remove(pos - 1);

               User u;
               try {
                  u = event.getJDA().getUserById(qt.getIdentifier());
               } catch (Exception var9) {
                  u = null;
               }

               event.replySuccess(
                  "Removed **"
                     + qt.getTrack().getInfo().getTitle()
                     + "** from the queue (requested by "
                     + (u == null ? "someone" : "**" + u.getName() + "**")
                     + ")"
               );
            } else {
               event.replyError("You cannot remove **" + qt.getTrack().getInfo().getTitle() + "** because you didn't add it!");
            }
         } else {
            event.replyError("Position must be a valid integer between 1 and " + handler.getQueueManager().getQueue().size() + "!");
         }
      }
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
      OptionMapping option = event.getOption("posicion");
      if (option != null) {
         AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
         if (handler.getQueueManager().getQueue().isEmpty()) {
            event.reply(event.getClient().getError() + "There is nothing in the queue!").setEphemeral(true).queue();
         } else {
            int pos;
            try {
               pos = Integer.parseInt(option.getAsString());
            } catch (NumberFormatException var11) {
               pos = 0;
            }

            if (pos >= 1 && pos <= handler.getQueueManager().getQueue().size()) {
               Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
               boolean isDJ = event.getMember().hasPermission(new Permission[]{Permission.MANAGE_SERVER});
               if (!isDJ) {
                  isDJ = event.getMember().getRoles().contains(event.getGuild().getRoleById(settings.getDjRoleId()));
               }

               QueuedTrack qt = handler.getQueueManager().getQueue().get(pos - 1);
               log.debug("Comparing {} to {}", qt.getUserData(), event.getUser().getIdLong());
               if (qt.getUserData().equals(event.getUser().getIdLong())) {
                  handler.getQueueManager().getQueue().remove(pos - 1);
                  event.reply(event.getClient().getSuccess() + "Removed **" + qt.getTrack().getInfo().getTitle() + "** from the queue").queue();
               } else if (isDJ) {
                  handler.getQueueManager().getQueue().remove(pos - 1);

                  User u;
                  try {
                     u = event.getJDA().getUserById(qt.getIdentifier());
                  } catch (Exception var10) {
                     u = null;
                  }

                  event.reply(
                        event.getClient().getSuccess()
                           + "Removed **"
                           + qt.getTrack().getInfo().getTitle()
                           + "** from the queue (requested by "
                           + (u == null ? "someone" : "**" + u.getName() + "**")
                           + ")"
                     )
                     .queue();
               } else {
                  event.reply(event.getClient().getError() + "You cannot remove **" + qt.getTrack().getInfo().getTitle() + "** because you didn't add it!")
                     .queue();
               }
            } else {
               event.reply(event.getClient().getError() + "Position must be a valid integer between 1 and " + handler.getQueueManager().getQueue().size() + "!")
                  .setEphemeral(true)
                  .queue();
            }
         }
      }
   }
}



