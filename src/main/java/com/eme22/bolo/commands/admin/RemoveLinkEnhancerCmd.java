package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.model.LinkEnhancer;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.Collections;
import java.util.Map;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class RemoveLinkEnhancerCmd extends AdminCommand {
   private final Bot bot;
   @ConfigProperty(name = "config.aliases.removelinkenhancer", defaultValue = "")
   String[] aliases = new String[0];

   public RemoveLinkEnhancerCmd(@Named("adminCategory") Category category, Bot bot) {
      super(category);
      this.name = "removelinkenhancer";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "removelinkenhancer",
         DiscordLocale.ENGLISH_US,
         "removelinkenhancer",
         DiscordLocale.SPANISH,
         "removermejoradordeenlaces",
         DiscordLocale.SPANISH_LATAM,
         "removermejoradordeenlaces"
      );
      this.help = "Removes a regular expression from the list of regular expressions of the link enhancer";
      this.descriptionLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "Removes a regular expression from the list of regular expressions of the link enhancer",
         DiscordLocale.ENGLISH_US,
         "Removes a regular expression from the list of regular expressions of the link enhancer",
         DiscordLocale.SPANISH,
         "Remueve una expresion regular de la lista de expresiones regulares del mejorador de enlaces",
         DiscordLocale.SPANISH_LATAM,
         "Remueve una expresion regular de la lista de expresiones regulares del mejorador de enlaces"
      );
      this.bot = bot;
      this.guildOnly = true;
      this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "id", "ID of the link Enhancer to delete").setRequired(true));
   }

   protected void execute(SlashCommandEvent event) {
      Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      int id = Integer.parseInt(event.getOption("id").getAsString());
      LinkEnhancer linkEnhancer = settings.getLinkEnhancers().stream().filter(linkEnhancer1 -> linkEnhancer1.getId() == id).findFirst().orElse(null);
      if (linkEnhancer == null) {
         event.reply(languageService.getMessage("removelinkenhancer.notfound", new Object[]{id})).queue();
      } else {
         settings.getLinkEnhancers().remove(linkEnhancer);
         settings.persist();
         event.reply(languageService.getMessage("removelinkenhancer.removed")).queue();
      }
   }

   protected void execute(CommandEvent event) {
      Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      if (event.getArgs().isEmpty()) {
         event.replyError(languageService.getMessage("removelinkenhancer.noargs"));
      } else {
         int id = Integer.parseInt(event.getArgs());
         LinkEnhancer linkEnhancer = settings.getLinkEnhancers().stream().filter(linkEnhancer1 -> linkEnhancer1.getId() == id).findFirst().orElse(null);
         if (linkEnhancer == null) {
            event.replyError(languageService.getMessage("removelinkenhancer.notfound", new Object[]{id}));
         } else {
            settings.getLinkEnhancers().remove(linkEnhancer);
            settings.persist();
            event.replySuccess(languageService.getMessage("removelinkenhancer.removed"));
         }
      }
   }
}



