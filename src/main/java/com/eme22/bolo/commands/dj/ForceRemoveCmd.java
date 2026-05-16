package com.eme22.bolo.commands.dj;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.commands.DJCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jdautilities.menu.OrderedMenu.Builder;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class ForceRemoveCmd extends DJCommand {
   @ConfigProperty(name = "config.aliases.forceremove", defaultValue = "")
   String[] aliases = new String[0];

   public ForceRemoveCmd(Bot bot, @Named("djCategory") Category category) {
      super(bot, category);
      this.name = "forceremove";
      this.help = "removes all entries by a user from the queue";
      this.arguments = "<user>";
      this.beListening = false;
      this.bePlaying = true;
      this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
   }

   @Override
   public void doCommand(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.replyError("You need to mention a user!");
      } else {
         AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
         if (handler.getQueueManager().getQueue().isEmpty()) {
            event.replyError("There is nothing in the queue!");
         } else {
            List<Member> found = FinderUtil.findMembers(event.getArgs(), event.getGuild());
            if (found.isEmpty()) {
               event.replyError("Unable to find the user!");
            } else if (found.size() <= 1) {
               User target = found.get(0).getUser();
               this.removeAllEntries(target, event);
            } else {
               Builder builder = new Builder();

               for (int i = 0; i < found.size() && i < 4; i++) {
                  Member member = found.get(i);
                  builder.addChoice("**" + member.getUser().getName() + "**#" + member.getUser().getDiscriminator());
               }

               ((Builder)((Builder)((Builder)builder.setSelection((msg, ix) -> this.removeAllEntries(found.get(ix - 1).getUser(), event))
                           .setText("Found multiple users:")
                           .setColor(event.getSelfMember().getColor())
                           .useNumbers()
                           .setUsers(new User[]{event.getAuthor()}))
                        .useCancelButton(true)
                        .setCancel(msg -> {})
                        .setEventWaiter(this.bot.getWaiter()))
                     .setTimeout(1L, TimeUnit.MINUTES))
                  .build()
                  .display(event.getChannel());
            }
         }
      }
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
   }

   private void removeAllEntries(User target, CommandEvent event) {
      int count = this.bot.getPlayerManager().getAudioHandler(event.getGuild()).getQueueManager().getQueue().removeAllFrom(target.getId());
      if (count == 0) {
         event.replyWarning("**" + target.getName() + "** doesn't have any songs in the queue!");
      } else {
         event.replySuccess("Successfully removed `" + count + "` entries from **" + target.getName() + "**#" + target.getDiscriminator() + ".");
      }
   }
}








