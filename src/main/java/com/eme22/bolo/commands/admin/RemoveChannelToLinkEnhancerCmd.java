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
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class RemoveChannelToLinkEnhancerCmd extends AdminCommand {
   private final Bot bot;
   @ConfigProperty(name = "config.aliases.removechanneltolinkenhancer", defaultValue = "")
   String[] aliases = new String[0];

   public RemoveChannelToLinkEnhancerCmd(@Named("adminCategory") Category category, Bot bot) {
      super(category);
      this.name = "removechanneltolinkenhancer";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "removechanneltolinkenhancer",
         DiscordLocale.ENGLISH_US,
         "removechanneltolinkenhancer",
         DiscordLocale.SPANISH,
         "removercanaldemejoradordeenlaces",
         DiscordLocale.SPANISH_LATAM,
         "removercanaldemejoradordeenlaces"
      );
      this.help = "Removes a channel to the list of channels where the link enhancer is active";
      this.descriptionLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "Removes a channel to the list of channels where the link enhancer is active",
         DiscordLocale.ENGLISH_US,
         "Removes a channel to the list of channels where the link enhancer is active",
         DiscordLocale.SPANISH,
         "Remueve un canal a la lista de canales en los que el mejorador de enlaces esta activo",
         DiscordLocale.SPANISH_LATAM,
         "Remueve un canal a la lista de canales en los que el mejorador de enlaces esta activo"
      );
      this.arguments = "<channel>";
      this.options = Collections.singletonList(
         new OptionData(OptionType.CHANNEL, "channel", "canal al que se le activara el mejorador de enlaces").setRequired(true)
      );
      this.bot = bot;
      this.guildOnly = true;
   }

   public void execute(SlashCommandEvent event) {
      Server s = this.bot.getSettingsManager().getSettings(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      GuildChannelUnion channel = event.getOption("channel").getAsChannel();
      if (channel.getType().isAudio()) {
         event.reply(event.getClient().getError() + " " + lang.getMessage("linkenhancer.notextchannel")).queue();
      } else {
         TextChannel textChannel = channel.asTextChannel();
         s.removeFromLinkEnhancerChannels(textChannel.getIdLong());
         s.persist();
         event.reply(event.getClient().getSuccess() + " " + lang.getMessage("linkenhancer.removed", new Object[]{textChannel.getAsMention()})).queue();
      }
   }

   public void execute(CommandEvent event) {
      Server s = this.bot.getSettingsManager().getSettings(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      if (event.getArgs().isEmpty()) {
         event.replyError(lang.getMessage("linkenhancerchannel.noargs"));
      } else {
         List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
         if (list.isEmpty()) {
            event.replyError(lang.getMessage("linkenhancer.notextchannelfound", new Object[]{event.getArgs()}));
         } else {
            TextChannel textChannel = list.get(0);
            s.removeFromLinkEnhancerChannels(textChannel.getIdLong());
            s.persist();
            event.replySuccess(lang.getMessage("linkenhancer.removed", new Object[]{textChannel.getAsMention()}));
         }
      }
   }
}











