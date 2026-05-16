package com.eme22.bolo.commands.owner;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import com.eme22.bolo.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.Arrays;
import java.util.Collections;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class SetstatusCmd extends OwnerCommand {
   @ConfigProperty(name = "config.aliases.shutdown", defaultValue = "")
   String[] aliases = new String[0];

   public SetstatusCmd() {
      this.name = "setstatus";
      this.help = "sets the status the bot displays";
      this.arguments = "<status>";
      this.guildOnly = false;
      this.options = Collections.singletonList(
         new OptionData(OptionType.STRING, "status", "Setea el status del bot")
            .addChoices(new Choice[0])
            .addChoices(
               Arrays.asList(new Choice("Online", "ONLINE"), new Choice("Idle", "IDLE"), new Choice("No Molestar", "DND"), new Choice("Invisible", "INVISIBLE"))
            )
            .setRequired(true)
      );
   }

   public void execute(SlashCommandEvent event) {
      try {
         String url = event.optString("status", null);
         OnlineStatus status = OnlineStatus.fromKey(url);
         if (status == OnlineStatus.UNKNOWN) {
            event.reply(event.getClient().getError() + " Please include one of the following statuses: `ONLINE`, `IDLE`, `DND`, `INVISIBLE`").queue();
         } else {
            event.getJDA().getPresence().setStatus(status);
            event.reply(event.getClient().getSuccess() + " Set the status to `" + status.getKey().toUpperCase() + "`").queue();
         }
      } catch (Exception var4) {
         event.reply(event.getClient().getError() + " The status could not be set!").queue();
      }
   }

   public void execute(CommandEvent event) {
      try {
         OnlineStatus status = OnlineStatus.fromKey(event.getArgs());
         if (status == OnlineStatus.UNKNOWN) {
            event.replyError("Please include one of the following statuses: `ONLINE`, `IDLE`, `DND`, `INVISIBLE`");
         } else {
            event.getJDA().getPresence().setStatus(status);
            event.replySuccess("Set the status to `" + status.getKey().toUpperCase() + "`");
         }
      } catch (Exception var3) {
         event.reply(event.getClient().getError() + " The status could not be set!");
      }
   }
}










