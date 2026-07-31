package com.eme22.bolo.commands.admin;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.language.LanguageService;
import com.jagrosh.jdautilities.command.Command.Category;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;

import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Singleton
@Slf4j
@Transactional
@ActivateRequestContext
public class EmptyChannelCmd extends AdminCommand {

  @ConfigProperty(name = "config.aliases.emptychannel", defaultValue = "")
  String[] aliases = new String[0];

  private final Bot bot;

  @Inject
  public EmptyChannelCmd(Bot bot, @Named("adminCategory") Category adminCategory) {
    super(adminCategory);
    this.bot = bot;

    this.name = "emptychannel";
    this.nameLocalization = Map.of(
        DiscordLocale.ENGLISH_UK, "emptychannel",
        DiscordLocale.ENGLISH_US, "emptychannel",
        DiscordLocale.SPANISH, "vaciarcanal",
        DiscordLocale.SPANISH_LATAM, "vaciarcanal");

    this.help = "Desconecta a todos los usuarios de un canal de voz";
    this.descriptionLocalization = Map.of(
        DiscordLocale.ENGLISH_UK, "Disconnect all users from a voice channel",
        DiscordLocale.ENGLISH_US, "Disconnect all users from a voice channel",
        DiscordLocale.SPANISH, "Desconecta a todos los usuarios de un canal de voz",
        DiscordLocale.SPANISH_LATAM, "Desconecta a todos los usuarios de un canal de voz");

    this.botPermissions = new Permission[] { Permission.VOICE_MOVE_OTHERS };

    OptionData channelOption = new OptionData(OptionType.CHANNEL, "canal",
        "Canal de voz del cual vas a desconectar a todos los usuarios")
        .setNameLocalizations(Map.of(
            DiscordLocale.ENGLISH_UK, "channel",
            DiscordLocale.ENGLISH_US, "channel",
            DiscordLocale.SPANISH, "canal",
            DiscordLocale.SPANISH_LATAM, "canal"))
        .setDescriptionLocalizations(Map.of(
            DiscordLocale.ENGLISH_UK, "Voice channel from which to disconnect all users",
            DiscordLocale.ENGLISH_US, "Voice channel from which to disconnect all users",
            DiscordLocale.SPANISH, "Canal de voz del cual vas a desconectar a todos los usuarios",
            DiscordLocale.SPANISH_LATAM, "Canal de voz del cual vas a desconectar a todos los usuarios"))
        .setChannelTypes(ChannelType.VOICE, ChannelType.STAGE)
        .setRequired(true);

    this.options = List.of(channelOption);
  }

  @Override
  protected void execute(SlashCommandEvent event) {
    LanguageService lang = bot.getSettingsManager().getLanguageService(event.getGuild());
    OptionMapping channelOption = event.getOption("canal");

    if (channelOption == null) {
      event.reply(lang.getErrorMessage("command.emptychannel.nodetect")).setEphemeral(true).queue();
      return;
    }

    AudioChannel voiceChannel = channelOption.getAsChannel().asAudioChannel();
    Member member = event.getMember();
    GuildVoiceState memberState = member != null ? member.getVoiceState() : null;

    if (memberState == null) {
      event.reply(lang.getErrorMessage("command.emptychannel.nodetect")).setEphemeral(true).queue();
      return;
    }

    if (!memberState.inAudioChannel()) {
      event.reply(lang.getErrorMessage("command.emptychannel.notinchannel")).setEphemeral(true).queue();
      return;
    }

    AudioChannel memberVoiceChannel = memberState.getChannel();
    AudioChannel afkChannel = event.getGuild().getAfkChannel();

    if (afkChannel != null && afkChannel.equals(memberVoiceChannel)) {
      event.reply(lang.getErrorMessage("command.emptychannel.afk")).setEphemeral(true).queue();
      return;
    }

    if (!voiceChannel.equals(memberVoiceChannel)) {
      event.reply(lang.getErrorMessage("command.emptychannel.differentchannel")).setEphemeral(true).queue();
      return;
    }

    voiceChannel.getMembers().forEach(m -> event.getGuild().moveVoiceMember(m, null).queue());

    event.reply("https://tenor.com/es-419/view/troll-dancing-dance-gif-7693210").queue();
  }

  @Override
  protected void execute(CommandEvent event) {
    LanguageService lang = bot.getSettingsManager().getLanguageService(event.getGuild());
    Member member = event.getMember();
    GuildVoiceState memberState = member != null ? member.getVoiceState() : null;

    if (memberState == null || !memberState.inAudioChannel()) {
      event.replyError(lang.getMessage("command.emptychannel.notinchannel"));
      return;
    }

    AudioChannel memberVoiceChannel = memberState.getChannel();
    AudioChannel afkChannel = event.getGuild().getAfkChannel();

    if (afkChannel != null && afkChannel.equals(memberVoiceChannel)) {
      event.replyError(lang.getMessage("command.emptychannel.afk"));
      return;
    }

    AudioChannel targetChannel;
    if (event.getArgs().isEmpty()) {
      targetChannel = memberVoiceChannel;
    } else {
      List<VoiceChannel> channels = FinderUtil.findVoiceChannels(event.getArgs(), event.getGuild());
      if (channels.isEmpty()) {
        event.replyError(lang.getMessage("command.emptychannel.nodetect"));
        return;
      }
      targetChannel = channels.get(0);
    }

    if (!targetChannel.equals(memberVoiceChannel)) {
      event.replyError(lang.getMessage("command.emptychannel.differentchannel"));
      return;
    }

    targetChannel.getMembers().forEach(m -> event.getGuild().moveVoiceMember(m, null).queue());
    event.reply("https://tenor.com/es-419/view/troll-dancing-dance-gif-7693210");
  }
}
