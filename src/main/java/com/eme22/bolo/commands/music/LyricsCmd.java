package com.eme22.bolo.commands.music;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.commands.MusicCommand;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jlyrics.Lyrics;
import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Nullable;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class LyricsCmd extends MusicCommand {
   @ConfigProperty(name = "config.aliases.lyrics", defaultValue = "")
   String[] aliases = new String[0];

   public LyricsCmd(Bot bot) {
      super(bot);
      this.name = "lyrics";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_UK, "lyrics", DiscordLocale.ENGLISH_US, "lyrics", DiscordLocale.SPANISH, "letra", DiscordLocale.SPANISH_LATAM, "letra"
      );
      this.arguments = "[cancion]";
      this.help = "shows the lyrics of a song";
      this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
      this.options = Collections.singletonList(new OptionData(OptionType.STRING, "cancion", "Busca la letra de la cancion").setRequired(false));
   }

   @Override
   public void doCommand(CommandEvent event) {
      String title;
      if (event.getArgs().isEmpty()) {
         AudioHandler sendingHandler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
         if (!sendingHandler.isMusicPlaying(event.getJDA())) {
            event.replyError("There must be music playing to use that!");
            return;
         }

         title = this.bot.getPlayerManager().getAudioHandler(event.getGuild()).getAudioPlayer().get().getTrack().getInfo().getTitle();
      } else {
         title = event.getArgs();
      }

      event.getChannel().sendTyping().queue();
      Lyrics lyrics = OtherUtil.getLyrics(title);
      if (lyrics == null) {
         event.replyError(
            "Lyrics for `" + title + "` could not be found!" + (event.getArgs().isEmpty() ? " Try entering the song name manually (`lyrics [song name]`)" : "")
         );
      } else {
         showLyrics(event, event.getSelfMember().getColor(), null, title, lyrics);
      }
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
      OptionMapping option = event.getOption("cancion");
      String title;
      if (option != null && !option.getAsString().isEmpty()) {
         title = option.getAsString();
      } else {
         AudioHandler sendingHandler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
         if (!sendingHandler.isMusicPlaying(event.getJDA())) {
            event.reply(event.getClient().getError() + " There must be music playing to use that!").setEphemeral(true).queue();
            return;
         }

         title = this.bot.getPlayerManager().getAudioHandler(event.getGuild()).getAudioPlayer().get().getTrack().getInfo().getTitle();
      }

      event.deferReply()
         .queue(
            interaction -> {
               Lyrics lyrics = OtherUtil.getLyrics(title);
               if (lyrics == null) {
                  interaction.editOriginal(
                        event.getClient().getError()
                           + "Lyrics for `"
                           + title
                           + "` could not be found!"
                           + (title.isEmpty() ? " Try entering the song name manually (`lyrics [song name]`)" : "")
                     )
                     .queue();
               } else {
                  this.showLyrics(event, event.getGuild().getSelfMember().getColor(), null, title, lyrics);
               }
            }
         );
   }

   public static void showLyrics(@Nullable CommandEvent event, Color color, TextChannel channel, String title, Lyrics lyrics) {
      EmbedBuilder eb = new EmbedBuilder().setAuthor(lyrics.getAuthor()).setColor(color).setTitle(lyrics.getTitle(), lyrics.getURL());
      if (lyrics.getContent().length() > 15000) {
         if (event == null) {
            channel.sendMessage("Lyrics for `" + title + "` found but likely not correct: " + lyrics.getURL()).complete();
         } else {
            event.replyWarning("Lyrics for `" + title + "` found but likely not correct: " + lyrics.getURL());
         }
      } else if (lyrics.getContent().length() > 2000) {
         String content = lyrics.getContent().trim();

         while (content.length() > 2000) {
            int index = content.lastIndexOf("\n\n", 2000);
            if (index == -1) {
               index = content.lastIndexOf("\n", 2000);
            }

            if (index == -1) {
               index = content.lastIndexOf(" ", 2000);
            }

            if (index == -1) {
               index = 2000;
            }

            if (event == null) {
               channel.sendMessageEmbeds(eb.setDescription(content.substring(0, index).trim()).build(), new MessageEmbed[0]).complete();
            } else {
               event.reply(eb.setDescription(content.substring(0, index).trim()).build());
            }

            content = content.substring(index).trim();
            eb.setAuthor(null).setTitle(null, null);
         }

         if (event == null) {
            channel.sendMessageEmbeds(eb.setDescription(content).build(), new MessageEmbed[0]).complete();
         } else {
            event.reply(eb.setDescription(content).build());
         }
      } else if (event == null) {
         channel.sendMessageEmbeds(eb.setDescription(lyrics.getContent()).build(), new MessageEmbed[0]).complete();
      } else {
         event.reply(eb.setDescription(lyrics.getContent()).build());
      }
   }

   private void showLyrics(SlashCommandEvent event, Color color, TextChannel channel, String title, Lyrics lyrics) {
      EmbedBuilder eb = new EmbedBuilder().setAuthor(lyrics.getAuthor()).setColor(color).setTitle(lyrics.getTitle(), lyrics.getURL());
      if (lyrics.getContent().length() > 15000) {
         event.reply(event.getClient().getError() + "Lyrics for `" + title + "` found but likely not correct: " + lyrics.getURL()).setEphemeral(true).queue();
      } else if (lyrics.getContent().length() > 2000) {
         String content = lyrics.getContent().trim();

         while (content.length() > 2000) {
            int index = content.lastIndexOf("\n\n", 2000);
            if (index == -1) {
               index = content.lastIndexOf("\n", 2000);
            }

            if (index == -1) {
               index = content.lastIndexOf(" ", 2000);
            }

            if (index == -1) {
               index = 2000;
            }

            if (event == null) {
               channel.sendMessageEmbeds(eb.setDescription(content.substring(0, index).trim()).build(), new MessageEmbed[0]).complete();
            } else {
               event.replyEmbeds(eb.setDescription(content.substring(0, index).trim()).build(), new MessageEmbed[0]).queue();
            }

            content = content.substring(index).trim();
            eb.setAuthor(null).setTitle(null, null);
         }

         if (event == null) {
            channel.sendMessageEmbeds(eb.setDescription(content).build(), new MessageEmbed[0]).complete();
         } else {
            event.replyEmbeds(eb.setDescription(content).build(), new MessageEmbed[0]).queue();
         }
      } else if (event == null) {
         channel.sendMessageEmbeds(eb.setDescription(lyrics.getContent()).build(), new MessageEmbed[0]).complete();
      } else {
         event.replyEmbeds(eb.setDescription(lyrics.getContent()).build(), new MessageEmbed[0]).queue();
      }
   }
}







