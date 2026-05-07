package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.Collections;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class GetBirthdaysCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.getbirthdays", defaultValue = "")
   String[] aliases = new String[0];
   private final Bot bot;

   public GetBirthdaysCmd(@Named("adminCategory") Category category, Bot bot) {
      super(category);
      this.name = "getbirthdays";
      this.help = "obtiene los cumpleaÃ±os del servidor argumento opcional: hoy";
      this.bot = bot;
      this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, "hoy", "obtiene los cumpleaÃ±os de hoy").setRequired(false));
   }

   protected void execute(SlashCommandEvent event) {
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (event.getOption("hoy") != null && event.getOption("hoy").getAsBoolean()) {
         event.replyEmbeds(this.bot.getBirthdayManager().getBirthdaysToday(event.getGuild()), new MessageEmbed[0]).queue();
      } else {
         event.replyEmbeds(this.bot.getBirthdayManager().getBirthdays(event.getGuild()), new MessageEmbed[0]).queue();
      }
   }

   protected void execute(CommandEvent event) {
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (event.getArgs().equalsIgnoreCase("hoy")) {
         event.getTextChannel().sendMessageEmbeds(this.bot.getBirthdayManager().getBirthdaysToday(event.getGuild()), new MessageEmbed[0]).queue();
      } else {
         event.getTextChannel().sendMessageEmbeds(this.bot.getBirthdayManager().getBirthdays(event.getGuild()), new MessageEmbed[0]).queue();
      }
   }
}


