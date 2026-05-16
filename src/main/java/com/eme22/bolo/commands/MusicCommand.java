package com.eme22.bolo.commands;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import com.eme22.bolo.Bot;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import jakarta.inject.Inject;
@Transactional
@ActivateRequestContext
public abstract class MusicCommand extends BaseCommand {
   protected final Bot bot;
   protected boolean bePlaying;
   protected boolean beListening;

   @Inject
   public MusicCommand(Bot bot) {
      this.bot = bot;
      this.guildOnly = true;
      this.category = new Category("Music");
   }

   @Override
   public void execute(CommandEvent event) {
      Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild().getIdLong());
      TextChannel tchannel = event.getGuild().getTextChannelById(settings.getTextChannelId());
      if (this.isTextChannelAllowed(event, tchannel, lang)) {
         switch (OtherUtil.isUserInVoice(event.getGuild(), settings, event.getMember())) {
            case 0:
               event.reply(lang.getMessage("command.admin.only.in.audio.channel", new Object[]{event.getClient().getError()}));
               return;
            case 2:
               event.reply(lang.getMessage("command.admin.only.in.afk.channel", new Object[]{event.getClient().getError()}));
               return;
            default:
               this.bot.getPlayerManager().setUpHandler(event.getGuild(), event.getMember().getVoiceState().getChannel().asVoiceChannel());
               if (!this.bePlaying || this.isMusicPlaying(event, lang)) {
                  if (!this.beListening || !this.shouldConnect(event) || this.isBotConnected(event, lang)) {
                     this.doCommand(event);
                  }
               }
         }
      }
   }

   @Override
   public void execute(SlashCommandEvent event) {
      Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild().getIdLong());
      TextChannel tchannel = event.getGuild().getTextChannelById(settings.getTextChannelId());
      if (this.isTextChannelAllowed(event, tchannel, lang)) {
         switch (OtherUtil.isUserInVoice(event.getGuild(), settings, event.getMember())) {
            case 0:
               event.reply(lang.getMessage("command.admin.only.in.audio.channel", new Object[]{event.getClient().getError()})).setEphemeral(true).queue();
               return;
            case 2:
               event.reply(lang.getMessage("command.admin.only.in.afk.channel", new Object[]{event.getClient().getError()})).setEphemeral(true).queue();
               return;
            default:
               this.bot.getPlayerManager().setUpHandler(event.getGuild(), event.getMember().getVoiceState().getChannel().asVoiceChannel());
               if (!this.bePlaying || this.isMusicPlaying(event, lang)) {
                  if (!this.beListening || !this.shouldConnect(event) || this.isBotConnected(event, lang)) {
                     this.doCommand(event);
                  }
               }
         }
      }
   }

   public boolean isTextChannelAllowed(CommandEvent event, TextChannel tchannel, LanguageService lang) {
      if (tchannel == null) {
         return true;
      } else if (!event.getTextChannel().getId().equals(tchannel.getId())) {
         event.getMessage().delete().queue();
         event.replyInDm(lang.getMessage("command.admin.only.in.text.channel", new Object[]{event.getClient().getError(), tchannel.getAsMention()}));
         return false;
      } else {
         return true;
      }
   }

   public boolean isTextChannelAllowed(SlashCommandEvent event, TextChannel tchannel, LanguageService lang) {
      if (tchannel == null) {
         return true;
      } else if (!event.getTextChannel().getId().equals(tchannel.getId())) {
         event.reply(lang.getMessage("command.admin.only.in.text.channel", new Object[]{event.getClient().getError(), tchannel.getAsMention()}))
            .setEphemeral(true)
            .queue();
         return false;
      } else {
         return true;
      }
   }

   public boolean isMusicPlaying(CommandEvent event, LanguageService lang) {
      if (!this.bot.getPlayerManager().getAudioHandler(event.getGuild().getIdLong()).isMusicPlaying(event.getJDA())) {
         event.reply(lang.getMessage("command.music.playing.none", new Object[]{event.getClient().getError()}));
         return false;
      } else {
         return true;
      }
   }

   public boolean isMusicPlaying(SlashCommandEvent event, LanguageService lang) {
      if (!this.bot.getPlayerManager().getAudioHandler(event.getGuild()).isMusicPlaying(event.getJDA())) {
         event.reply(lang.getMessage("command.music.playing.none", new Object[]{event.getClient().getError()})).setEphemeral(true).queue();
         return false;
      } else {
         return true;
      }
   }

   public boolean isBotConnected(CommandEvent event, LanguageService lang) {
      if (event.getGuild().getSelfMember().getVoiceState() == null) {
         return false;
      } else if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
         Member member = event.getMember();
         GuildVoiceState userState = member.getVoiceState();
         if (userState != null && userState.getChannel() != null && userState.inAudioChannel()) {
            try {
               event.getGuild().getJDA().getDirectAudioController().connect(userState.getChannel());
               return true;
            } catch (PermissionException var6) {
               event.reply(lang.getMessage("command.admin.bot.no.connect", new Object[]{event.getClient().getError(), userState.getChannel().getAsMention()}));
               return false;
            }
         } else {
            event.reply(lang.getMessage("command.admin.bot.only.in.audio.channel", new Object[]{event.getClient().getError()}));
            return false;
         }
      } else {
         return true;
      }
   }

   public boolean isBotConnected(SlashCommandEvent event, LanguageService languageService) {
      if (event.getGuild() == null) {
         return false;
      } else if (event.getGuild().getSelfMember().getVoiceState() == null) {
         return false;
      } else if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
         Member member = event.getMember();
         if (member == null) {
            return false;
         } else {
            GuildVoiceState userState = member.getVoiceState();
            if (userState != null && userState.getChannel() != null && userState.inAudioChannel()) {
               try {
                  event.getGuild().getJDA().getDirectAudioController().connect(userState.getChannel());
                  return true;
               } catch (PermissionException var6) {
                  event.reply(
                        languageService.getMessage(
                           "command.admin.bot.no.connect", new Object[]{event.getClient().getError(), userState.getChannel().getAsMention()}
                        )
                     )
                     .setEphemeral(true)
                     .queue();
                  return false;
               }
            } else {
               event.reply(languageService.getMessage("command.admin.bot.only.in.audio.channel", new Object[]{event.getClient().getError()}))
                  .setEphemeral(true)
                  .queue();
               return false;
            }
         }
      } else {
         return true;
      }
   }

   public abstract void doCommand(CommandEvent event);

   public abstract void doCommand(SlashCommandEvent event);

   public boolean shouldConnect(CommandEvent event) {
      return true;
   }

   public boolean shouldConnect(SlashCommandEvent event) {
      return true;
   }
}





