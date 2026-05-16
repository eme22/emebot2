package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.stats.StatsService;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import java.util.Collections;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class AvatarCmd extends BaseCommand {
   private final StatsService statsService;
   private final Bot bot;

   @Inject
   public AvatarCmd(Bot bot, StatsService statsService, @ConfigProperty(name = "config.aliases.avatar", defaultValue = "") String[] aliases) {
      this.name = "avatar";
      this.help = "muestra el avatar del usuario nombrado";
      this.arguments = "<user>";
      this.guildOnly = true;
      this.statsService = statsService;
      this.aliases = aliases;
      this.bot = bot;
      this.options = Collections.singletonList(new OptionData(OptionType.USER, "usuario", "Seleccione al usuario al que ver su avatar.").setRequired(true));
   }

   public void execute(SlashCommandEvent event) {
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      OptionMapping option = event.getOption("usuario");
      User user = option.getAsUser();
      Member member = option.getAsMember();
      OtherUtil.sendAvatar(event, user, member, this.statsService, languageService, this.bot);
   }

   public void execute(CommandEvent event) {
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      if (event.getArgs().isEmpty()) {
         event.replyError(languageService.getMessage("command.avatar.name.empty"));
      } else {
         List<Member> member = FinderUtil.findMembers(event.getArgs(), event.getGuild());
         if (member.isEmpty()) {
            try {
               long id = Long.parseLong(event.getArgs().replaceAll("[^0-9]", ""));
               event.getJDA().retrieveUserById(id).queue(user -> {
                  OtherUtil.sendAvatar(event, user, event.getGuild().getMember(user), this.statsService, languageService, this.bot);
               }, throwable -> {
                  event.replyError(languageService.getMessage("command.avatar.not.found"));
               });
            } catch (NumberFormatException e) {
               event.replyError(languageService.getMessage("command.avatar.not.found"));
            }
         } else {
            Member member1 = member.get(0);
            OtherUtil.sendAvatar(event, member1.getUser(), member1, this.statsService, languageService, this.bot);
         }
      }
   }
}










