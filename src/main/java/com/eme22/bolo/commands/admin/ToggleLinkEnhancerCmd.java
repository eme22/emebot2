package com.eme22.bolo.commands.admin;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.Map;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class ToggleLinkEnhancerCmd extends AdminCommand {
   private final Bot bot;
   @ConfigProperty(name = "config.aliases.togglelinkenhancer", defaultValue = "")
   String[] aliases = new String[0];

   public ToggleLinkEnhancerCmd(@Named("adminCategory") Category category, Bot bot) {
      super(category);
      this.name = "togglelinkenhancer";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "togglelinkenhancer",
         DiscordLocale.ENGLISH_US,
         "togglelinkenhancer",
         DiscordLocale.SPANISH,
         "conmutarmejoradordeenlaces",
         DiscordLocale.SPANISH_LATAM,
         "conmutarmejoradordeenlaces"
      );
      this.help = "Activa o desactiva el link enhancer";
      this.descriptionLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "Activates or deactivates the link enhancer",
         DiscordLocale.ENGLISH_US,
         "Activates or deactivates the link enhancer",
         DiscordLocale.SPANISH,
         "Activa o desactiva el mejorador de enlaces",
         DiscordLocale.SPANISH_LATAM,
         "Activa o desactiva el mejorador de enlaces"
      );
      this.arguments = "<on/off>";
      this.bot = bot;
      this.guildOnly = true;
   }

   public void execute(SlashCommandEvent event) {
      Server s = this.bot.getSettingsManager().getSettings(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      s.setLinkEnhancerEnabled(!s.getLinkEnhancerEnabled());
      s.persist();
      event.reply(event.getClient().getSuccess() + " " + lang.getMessage("linkenhancer." + (s.getLinkEnhancerEnabled() ? "enabled" : "disabled"))).queue();
   }

   public void execute(CommandEvent event) {
      Server s = this.bot.getSettingsManager().getSettings(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      s.setLinkEnhancerEnabled(!s.getLinkEnhancerEnabled());
      s.persist();
      event.replySuccess(lang.getMessage("linkenhancer." + (s.getLinkEnhancerEnabled() ? "enabled" : "disabled")));
   }
}











