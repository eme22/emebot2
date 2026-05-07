package com.eme22.bolo.commands.dj;

import jakarta.inject.Named;

import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.commands.DJCommand;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.utils.FormatUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import java.util.Collections;
import java.util.Optional;
import lombok.Generated;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Slf4j
public class VolumeCmd extends DJCommand {
   @Generated
   
   @ConfigProperty(name = "config.aliases.volume", defaultValue = "")
   String[] aliases = new String[0];

   public VolumeCmd(Bot bot, @Named("djCategory") Category category) {
      super(bot, category);
      this.name = "volume";
      this.help = "sets or shows volume";
      this.arguments = "[0-999]";
      this.options = Collections.singletonList(
         new OptionData(OptionType.INTEGER, "volumen", "Setea el volumen seleccionado.").setMinValue(0L).setMaxValue(999L).setRequired(false)
      );
   }

   @Override
   public void doCommand(CommandEvent event) {
      LavalinkPlayer player = this.getPlayer(event);
      if (player != null) {
         int volume = player.getVolume();
         if (event.getArgs().isEmpty()) {
            event.reply(FormatUtil.volumeIcon(volume) + " El volumen es `" + volume + "`");
         } else {
            this.setVolume(event, player, volume, event.getArgs());
         }
      }
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
      OptionMapping option = event.getOption("volumen");
      LavalinkPlayer player = this.getPlayer(event);
      if (player != null) {
         int volume = player.getVolume();
         if (option == null) {
            event.reply(FormatUtil.volumeIcon(volume) + " El volumen es `" + volume + "`").queue();
         } else {
            this.setVolume(event, player, volume, option.getAsString());
         }
      }
   }

   private LavalinkPlayer getPlayer(CommandEvent event) {
      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      Optional<LavalinkPlayer> optionalLavalinkPlayer = handler.getAudioPlayer();
      if (optionalLavalinkPlayer.isEmpty()) {
         log.warn("Player is empty, trying to get it again");
         optionalLavalinkPlayer = handler.getAudioPlayer();
         if (optionalLavalinkPlayer.isEmpty()) {
            log.error("Player is still empty, returning");
            return null;
         }
      }

      return optionalLavalinkPlayer.get();
   }

   private LavalinkPlayer getPlayer(SlashCommandEvent event) {
      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      Optional<LavalinkPlayer> optionalLavalinkPlayer = handler.getAudioPlayer();
      if (optionalLavalinkPlayer.isEmpty()) {
         log.warn("Player is empty, trying to get it again");
         optionalLavalinkPlayer = handler.getAudioPlayer();
         if (optionalLavalinkPlayer.isEmpty()) {
            log.error("Player is still empty, returning");
            return null;
         }
      }

      return optionalLavalinkPlayer.get();
   }

   private void setVolume(CommandEvent event, LavalinkPlayer player, int currentVolume, String volumeArg) {
      int newVolume;
      try {
         newVolume = Integer.parseInt(volumeArg);
      } catch (NumberFormatException var7) {
         event.replyError(" El volumen debe ser un numero!");
         return;
      }

      if (newVolume >= 0 && newVolume <= 999) {
         player.setVolume(newVolume).subscribe(player1 -> {
            Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
            settings.setVolume(newVolume);
            settings.persist();
            event.reply(FormatUtil.volumeIcon(newVolume) + " Volumen cambiado de `" + currentVolume + "` a `" + newVolume + "`");
         });
      } else {
         event.replyError(" El volumen debe estar entre  0 y 999!");
      }
   }

   private void setVolume(SlashCommandEvent event, LavalinkPlayer player, int currentVolume, String volumeArg) {
      int newVolume = Integer.parseInt(volumeArg);
      if (newVolume >= 0 && newVolume <= 999) {
         player.setVolume(newVolume).subscribe(player1 -> {
            Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
            settings.setVolume(newVolume);
            settings.persist();
            event.reply(FormatUtil.volumeIcon(newVolume) + " Volumen cambiado de `" + currentVolume + "` a `" + newVolume + "`").queue();
         });
      } else {
         event.reply(event.getClient().getError() + " El volumen debe estar entre  0 y 999!").setEphemeral(true).queue();
      }
   }
}




