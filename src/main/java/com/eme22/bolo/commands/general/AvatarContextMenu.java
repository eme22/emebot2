package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import com.eme22.bolo.Bot;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.stats.StatsService;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.UserContextMenu;
import com.jagrosh.jdautilities.command.UserContextMenuEvent;
import java.util.Map;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class AvatarContextMenu extends UserContextMenu {
   private final StatsService statsService;
   private final Bot bot;

   @Inject
   public AvatarContextMenu(Bot bot, StatsService statsService, @ConfigProperty(name = "config.aliases.avatar", defaultValue = "") String[] aliases) {
      this.name = "See this user avatar";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_US,
         "See this user avatar",
         DiscordLocale.SPANISH,
         "Ver el avatar de este usuario",
         DiscordLocale.SPANISH_LATAM,
         "Ver el avatar de este usuario",
         DiscordLocale.ENGLISH_UK,
         "See this user avatar"
      );
      this.guildOnly = true;
      this.statsService = statsService;
      this.bot = bot;
   }

   public void execute(UserContextMenuEvent event) {
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      OtherUtil.sendAvatar(event, event.getTargetMember(), this.statsService, languageService, this.bot);
   }
}










