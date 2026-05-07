package com.eme22.bolo.commands.owner;

import com.eme22.bolo.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.Collections;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class SetnameCmd extends OwnerCommand {
   @ConfigProperty(name = "config.aliases.setname", defaultValue = "")
   String[] aliases = new String[0];

   public SetnameCmd() {
      this.name = "setname";
      this.help = "sets the name of the bot";
      this.arguments = "<name>";
      this.guildOnly = false;
      this.options = Collections.singletonList(new OptionData(OptionType.STRING, "name", "Setea el nombre del bot"));
   }

   protected void execute(SlashCommandEvent event) {
      String url = event.optString("name", null);

      try {
         String oldname = event.getJDA().getSelfUser().getName();
         event.getJDA().getSelfUser().getManager().setName(url).complete(false);
         event.reply(event.getClient().getSuccess() + " Name changed from `" + oldname + "` to `" + url + "`").queue();
      } catch (RateLimitedException var4) {
         event.reply(event.getClient().getError() + " Name can only be changed twice per hour!").queue();
      } catch (Exception var5) {
         event.reply(event.getClient().getError() + " That name is not valid!").queue();
      }
   }

   protected void execute(CommandEvent event) {
      try {
         String oldname = event.getSelfUser().getName();
         event.getSelfUser().getManager().setName(event.getArgs()).complete(false);
         event.reply(event.getClient().getSuccess() + " Name changed from `" + oldname + "` to `" + event.getArgs() + "`");
      } catch (RateLimitedException var3) {
         event.reply(event.getClient().getError() + " Name can only be changed twice per hour!");
      } catch (Exception var4) {
         event.reply(event.getClient().getError() + " That name is not valid!");
      }
   }
}


