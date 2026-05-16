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

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.EmbedBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;
import java.util.stream.Collectors;

@Singleton
@Transactional
@ActivateRequestContext
public class AddChannelToLinkEnhancerCmd extends AdminCommand {
   private final Bot bot;
   @ConfigProperty(name = "config.aliases.addchanneltolinkenhancer", defaultValue = "")
   String[] aliases = new String[0];

   public AddChannelToLinkEnhancerCmd(@Named("adminCategory") Category category, Bot bot) {
      super(category);
      this.name = "addchanneltolinkenhancer";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "addchanneltolinkenhancer",
         DiscordLocale.ENGLISH_US,
         "addchanneltolinkenhancer",
         DiscordLocale.SPANISH,
         "agregarcanalamejoradordeenlaces",
         DiscordLocale.SPANISH_LATAM,
         "agregarcanalamejoradordeenlaces"
      );
      this.help = "Agrega un canal a la lista de canales en los que el mejorador de enlaces esta activo";
      this.descriptionLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "Adds a channel to the list of channels where the link enhancer is active",
         DiscordLocale.ENGLISH_US,
         "Adds a channel to the list of channels where the link enhancer is active",
         DiscordLocale.SPANISH,
         "Agrega un canal a la lista de canales en los que el mejorador de enlaces esta activo",
         DiscordLocale.SPANISH_LATAM,
         "Agrega un canal a la lista de canales en los que el mejorador de enlaces esta activo"
      );
      this.arguments = "[channel]";
      this.options = Collections.singletonList(
         new OptionData(OptionType.CHANNEL, "channel", "canal al que se le activara el mejorador de enlaces").setRequired(false)
      );
      this.bot = bot;
      this.guildOnly = true;
   }

   public void execute(SlashCommandEvent event) {
      Server s = this.bot.getSettingsManager().getSettings(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      
      if (event.getOption("channel") == null) {
         sendManagerMenu(event, s, lang);
         return;
      }

      GuildChannelUnion channel = event.getOption("channel").getAsChannel();
      if (channel.getType().isAudio()) {
         event.reply(event.getClient().getError() + " " + lang.getMessage("linkenhancer.notextchannel")).queue();
      } else {
         TextChannel textChannel = channel.asTextChannel();
         s.addToLinkEnhancerChannels(textChannel.getIdLong());
         s.persist();
         event.reply(event.getClient().getSuccess() + " " + lang.getMessage("linkenhancer.added", new Object[]{textChannel.getAsMention()})).queue();
      }
   }

   private void sendManagerMenu(SlashCommandEvent event, Server s, LanguageService lang) {
      EmbedBuilder eb = new EmbedBuilder()
              .setTitle(lang.getMessage("linkenhancer.manage.title"))
              .setColor(event.getGuild().getSelfMember().getColor());

      String current = s.getLinkEnhancerChannels().stream()
              .map(id -> "<#" + id + ">")
              .collect(Collectors.joining(", "));

      if (current.isEmpty()) current = lang.getMessage("command.music.queue.none"); // Or similar empty message

      eb.setDescription(lang.getMessage("linkenhancer.manage.current", new Object[]{current}));

      EntitySelectMenu menu = EntitySelectMenu.create("linkenhancer:add-channels", EntitySelectMenu.SelectTarget.CHANNEL)
              .setPlaceholder(lang.getMessage("linkenhancer.manage.placeholder"))
              .setMinValues(1)
              .setMaxValues(10)
              .build();

      event.replyEmbeds(eb.build())
              .setComponents(ActionRow.of(menu))
              .setEphemeral(true)
              .queue();
   }

   private void sendManagerMenu(CommandEvent event, Server s, LanguageService lang) {
      EmbedBuilder eb = new EmbedBuilder()
              .setTitle(lang.getMessage("linkenhancer.manage.title"))
              .setColor(event.getGuild().getSelfMember().getColor());

      String current = s.getLinkEnhancerChannels().stream()
              .map(id -> "<#" + id + ">")
              .collect(Collectors.joining(", "));

      if (current.isEmpty()) current = lang.getMessage("command.music.queue.none");

      eb.setDescription(lang.getMessage("linkenhancer.manage.current", new Object[]{current}));

      EntitySelectMenu menu = EntitySelectMenu.create("linkenhancer:add-channels", EntitySelectMenu.SelectTarget.CHANNEL)
              .setPlaceholder(lang.getMessage("linkenhancer.manage.placeholder"))
              .setMinValues(1)
              .setMaxValues(10)
              .build();

      event.getChannel().sendMessageEmbeds(eb.build())
              .setComponents(ActionRow.of(menu))
              .queue();
   }

   public void execute(CommandEvent event) {
      Server s = this.bot.getSettingsManager().getSettings(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      if (event.getArgs().isEmpty()) {
         sendManagerMenu(event, s, lang);
      } else {
         List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
         if (list.isEmpty()) {
            event.replyError(lang.getMessage("linkenhancer.notextchannelfound", new Object[]{event.getArgs()}));
         } else {
            TextChannel textChannel = list.get(0);
            s.addToLinkEnhancerChannels(textChannel.getIdLong());
            s.persist();
            event.replySuccess(lang.getMessage("linkenhancer.added", new Object[]{textChannel.getAsMention()}));
         }
      }
   }
}











