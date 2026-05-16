package com.eme22.bolo.commands.admin;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Named;

import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.language.LanguageService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Slf4j
@Transactional
@ActivateRequestContext
public class SetServerLangCmd extends AdminCommand {
   @Generated
   
   private static List<SelectOption> getLanguageOptions() {
      return List.of(
         SelectOption.of("Español", "es").withDescription("Selecciona el idioma Español").withEmoji(Emoji.fromUnicode("\ud83c\uddea\ud83c\uddf8")),
         SelectOption.of("English", "en").withDescription("Select English language").withEmoji(Emoji.fromUnicode("\ud83c\uddfa\ud83c\uddf8"))
      );
   }
   private Bot bot;
   @ConfigProperty(name = "config.aliases.setlang", defaultValue = "")
   protected String[] aliases;

   public SetServerLangCmd(@Named("adminCategory") Category category, Bot bot) {
      super(category);
      this.name = "setlang";
      this.help = "Sets the server language / Establece el idioma del servidor";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_US, "setlang", DiscordLocale.SPANISH, "idioma", DiscordLocale.SPANISH_LATAM, "idioma", DiscordLocale.ENGLISH_UK, "setlang"
      );
      this.bot = bot;
   }

   public void execute(CommandEvent event) {
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      StringSelectMenu menu = StringSelectMenu.create("setlang")
         .addOptions(getLanguageOptions())
         .setDefaultValues(new String[]{languageService.getLocale().getLanguage()})
         .build();
      event.getTextChannel().sendMessage(languageService.getMessage("command.setlang.message"))
         .setComponents(ActionRow.of(menu))
         .queue();
   }

   public void execute(SlashCommandEvent event) {
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      StringSelectMenu menu = StringSelectMenu.create("setlang")
         .addOptions(getLanguageOptions())
         .setDefaultValues(new String[]{languageService.getLocale().getLanguage()})
         .build();
      log.info("Languages {}", this.bot.getSettingsManager().getLanguageServices());
      log.info("Current Language {}", languageService);
      log.info("Language Test: {}", languageService.getMessage("command.setlang.message"));
      event.reply(languageService.getMessage("command.setlang.message"))
         .addComponents(ActionRow.of(menu))
         .queue();
   }
}












