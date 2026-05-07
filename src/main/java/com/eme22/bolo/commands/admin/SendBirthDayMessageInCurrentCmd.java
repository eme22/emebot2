package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class SendBirthDayMessageInCurrentCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.forcebirthdaycurrent", defaultValue = "")
   String[] aliases = new String[0];
   private final Bot bot;

   public SendBirthDayMessageInCurrentCmd(@Named("adminCategory") Category category, Bot bot) {
      super(category);
      this.name = "forcebirthdaycurrent";
      this.help = "envia un mensaje de cumpleaÃ±os al servidor en el canal actual";
      this.bot = bot;
   }

   protected void execute(SlashCommandEvent event) {
      this.bot.getBirthdayManager().remindBirthdays(this.bot, event.getTextChannel());
      event.reply("Mensaje de cumpleaÃ±os enviado").setEphemeral(true).queue();
   }

   protected void execute(CommandEvent event) {
      this.bot.getBirthdayManager().remindBirthdays(this.bot, event.getTextChannel());
      event.getChannel()
         .sendMessage(event.getClient().getSuccess() + " Mensaje de cumpleaÃ±os enviado ")
         .queue(m -> m.delete().queueAfter(5L, TimeUnit.SECONDS));
   }
}



