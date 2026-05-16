package com.eme22.bolo.commands.dj;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.DJCommand;
import com.eme22.bolo.model.RepeatMode;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.language.LanguageService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.Collections;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class RepeatCmd extends DJCommand {
   @ConfigProperty(name = "config.aliases.repeat", defaultValue = "")
   String[] aliases = new String[0];

   public RepeatCmd(Bot bot, @Named("djCategory") Category category) {
      super(bot, category);
      this.name = "repeat";
      this.help = "re-adds music to the queue when finished";
      this.arguments = "[off|all|single]";
      this.guildOnly = true;
      this.options = Collections.singletonList(new OptionData(OptionType.STRING, "modo", "[off-all-single]").setRequired(false));
   }

   @Override
   public void execute(CommandEvent event) {
      String args = event.getArgs();
      Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
      RepeatMode value;
      if (args.isEmpty()) {
         if (settings.getRepeatMode() == RepeatMode.OFF) {
            value = RepeatMode.ALL;
         } else {
            value = RepeatMode.OFF;
         }
      } else if (args.equalsIgnoreCase("false") || args.equalsIgnoreCase("off")) {
         value = RepeatMode.OFF;
      } else if (!args.equalsIgnoreCase("true") && !args.equalsIgnoreCase("on") && !args.equalsIgnoreCase("all")) {
         if (!args.equalsIgnoreCase("one") && !args.equalsIgnoreCase("single")) {
            event.replyError("Valid options are `off`, `all` or `single` (or leave empty to toggle between `off` and `all`)");
            return;
         }

         value = RepeatMode.SINGLE;
      } else {
         value = RepeatMode.ALL;
      }

      settings.setRepeatMode(value);
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      event.replySuccess("Repeat mode is now `" + lang.getMessage("music.repeat." + value.getKey()) + "`");
   }

   @Override
   public void execute(SlashCommandEvent event) {
      OptionMapping option = event.getOption("modo");
      Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
      String args = null;
      if (option != null) {
         args = option.getAsString();
      }

      RepeatMode value;
      if (option == null) {
         if (settings.getRepeatMode() == RepeatMode.OFF) {
            value = RepeatMode.ALL;
         } else {
            value = RepeatMode.OFF;
         }
      } else if (args.equalsIgnoreCase("false") || args.equalsIgnoreCase("off")) {
         value = RepeatMode.OFF;
      } else if (!args.equalsIgnoreCase("true") && !args.equalsIgnoreCase("on") && !args.equalsIgnoreCase("all")) {
         if (!args.equalsIgnoreCase("one") && !args.equalsIgnoreCase("single")) {
            event.reply(event.getClient().getError() + "Valid options are `off`, `all` or `single` (or leave empty to toggle between `off` and `all`)").queue();
            return;
         }

         value = RepeatMode.SINGLE;
      } else {
         value = RepeatMode.ALL;
      }

      settings.setRepeatMode(value);
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      event.reply(event.getClient().getSuccess() + "Repeat mode is now `" + lang.getMessage("music.repeat." + value.getKey()) + "`").queue();
   }

   @Override
   public void doCommand(CommandEvent event) {
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
   }
}











