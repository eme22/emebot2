package com.eme22.bolo.commands.admin;

import java.util.List;
import java.util.Map;

import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Singleton
@Slf4j
@Transactional
@ActivateRequestContext
public class EmptyChannelCmd extends AdminCommand {

  public EmptyChannelCmd(Category adminCategory) {
    super(adminCategory);

    this.name = "emptychannel";
    this.nameLocalization = Map.of(
        DiscordLocale.ENGLISH_UK,
        "emptychannel",
        DiscordLocale.ENGLISH_US,
        "emptychannel",
        DiscordLocale.SPANISH,
        "vaciarcanal",
        DiscordLocale.SPANISH_LATAM,
        "vaciarcanal");

    this.help = "Desconecta a todos los usuarios de un canal de voz";
    this.descriptionLocalization = Map.of(
        DiscordLocale.ENGLISH_UK,
        "Disconnect all users from a voice channel",
        DiscordLocale.ENGLISH_US,
        "Disconnect all users from a voice channel",
        DiscordLocale.SPANISH,
        "Desconecta a todos los usuarios de un canal de voz",
        DiscordLocale.SPANISH_LATAM,
        "Desconecta a todos los usuarios de un canal de voz");

    this.options = List.of(
        new OptionData(OptionType.CHANNEL, "canal", "Canal de voz que vas a desconectar a todos los usuarios")
            .setRequired(true));
  }

  @Override
  protected void execute(SlashCommandEvent event) {
    AudioChannel voiceChannel = event.getOption("canal").getAsChannel().asAudioChannel();

    Member member = event.getMember();
    GuildVoiceState memberState = member.getVoiceState();

    if (memberState == null) {
      event.reply("El bot no puede detectar el canal de voz").setEphemeral(true);
      return;
    }

    if (!memberState.inAudioChannel()) {
      event.reply("No estás conectado a un canal de voz").setEphemeral(true);
      return;
    }

    VoiceChannel memberVoiceChannel = memberState.getChannel().asVoiceChannel();
    VoiceChannel afkChannel = event.getGuild().getAfkChannel();

    if (afkChannel != null && afkChannel.equals(memberVoiceChannel)) {
      event.reply("Estás conectado a un canal AFK, conéctate a un canal normal e inténtalo de nuevo.")
          .setEphemeral(true);
      return;
    }

    if (!voiceChannel.equals(memberVoiceChannel)) {
      event.reply("Debes estar conectado al mismo canal que vas a vaciar.").setEphemeral(true);
      return;
    }

    voiceChannel.getMembers().forEach(m -> m.getGuild().getAudioManager().openAudioConnection(null));

    event.reply("https://tenor.com/es-419/view/troll-dancing-dance-gif-7693210");
    return;
  }

}
