package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class SetBirthdayTemplateMessageCmd extends AdminCommand {
   protected final Bot bot;
   @ConfigProperty(name = "config.aliases.birthdaytemplate", defaultValue = "")
   String[] aliases = new String[0];

   public SetBirthdayTemplateMessageCmd(Bot bot, @Named("adminCategory") Category category) {
      super(category);
      this.bot = bot;
      this.name = "birthdaytemplate";
      this.help = "establece el mensaje de cumpleaÃ±os, estructura: [ header ] [ footer ]";
      this.arguments = "[header] [footer]";
      this.options = Arrays.asList(
         new OptionData(OptionType.STRING, "header", "mensaje de cumpleaÃ±os").setRequired(true),
         new OptionData(OptionType.STRING, "footer", "mensaje de cumpleaÃ±os").setRequired(true)
      );
   }

   protected void execute(SlashCommandEvent event) {
      OptionMapping headerOption = event.getOption("header");
      OptionMapping footerOption = event.getOption("footer");
      if (headerOption != null && footerOption != null) {
         String header = headerOption.getAsString();
         String footer = footerOption.getAsString();
         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         s.setBirthdayTemplateHeader(header);
         s.setBirthdayTemplateFooter(footer);
         s.persist();
         event.reply(event.getClient().getSuccess() + " Mensaje de cumpleaÃ±os actualizado!").queue();
      }
   }

   protected void execute(CommandEvent event) {
      Pattern pattern = Pattern.compile("\\[(.*?)\\]");
      Matcher matcher = pattern.matcher(event.getArgs());
      if (!matcher.find()) {
         event.replyError("Por favor, incluya un mensaje de cumpleaÃ±os y un pie de pÃ¡gina.");
      } else {
         String header = matcher.group(1);
         if (!matcher.find()) {
            event.replyError("Por favor, incluya un mensaje de cumpleaÃ±os y un pie de pÃ¡gina.");
         } else {
            String footer = matcher.group(1);
            Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
            s.setBirthdayTemplateHeader(header);
            s.setBirthdayTemplateFooter(footer);
            s.persist();
            event.replySuccess("Mensaje de cumpleaÃ±os actualizado!");
         }
      }
   }
}



