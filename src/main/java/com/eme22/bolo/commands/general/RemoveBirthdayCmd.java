package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class RemoveBirthdayCmd extends BaseCommand {
   Bot bot;
   @ConfigProperty(name = "config.aliases.removebirthday", defaultValue = "")
   String[] aliases = new String[0];

   public RemoveBirthdayCmd(Bot bot) {
      this.bot = bot;
      this.name = "removebirthday";
      this.help = "borra tu cumpleaÃ±os del servidor";
      this.guildOnly = true;
   }

   protected void execute(SlashCommandEvent event) {
      Server s = this.bot.getSettingsManager().getSettings(event.getGuild());
      s.removeBirthDay(event.getUser().getIdLong());
      s.persist();
      event.reply(event.getClient().getSuccess() + " Se ha borrado tu cumpleaÃ±os").setEphemeral(true).queue();
   }

   protected void execute(CommandEvent event) {
      Server s = this.bot.getSettingsManager().getSettings(event.getGuild());
      s.removeBirthDay(event.getMember().getUser().getIdLong());
      s.persist();
      event.replySuccess(" Se ha borrado tu cumpleaÃ±os");
   }
}


