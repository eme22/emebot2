package com.eme22.bolo.commands;

import com.eme22.bolo.Bot;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public abstract class DJCommand extends MusicCommand {
   @Inject
   public DJCommand(Bot bot, @Named("djCategory") Category dj) {
      super(bot);
      this.category = dj;
   }

   @Override
   protected void execute(SlashCommandEvent event) {
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
                  if (!this.beListening || this.isBotConnected(event, lang)) {
                     if (!this.isDJ(event)) {
                        event.reply(lang.getMessage("command.dj.error", new Object[]{event.getClient().getError()})).setEphemeral(true).queue();
                     } else {
                        this.doCommand(event);
                     }
                  }
               }
         }
      }
   }

   boolean isDJ(SlashCommandEvent event) {
      if (event.getUser().getId().equals(event.getClient().getOwnerId())) {
         return true;
      } else if (event.getUser().equals(event.getGuild().getOwner().getUser())) {
         return true;
      } else {
         Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
         Role admin = event.getGuild().getRoleById(settings.getAdminRoleId());
         if (event.getMember().getRoles().contains(admin)) {
            return true;
         } else {
            Role dj = event.getGuild().getRoleById(settings.getDjRoleId());
            return dj != null && (event.getMember().getRoles().contains(dj) || dj.getIdLong() == event.getMember().getIdLong());
         }
      }
   }
}
