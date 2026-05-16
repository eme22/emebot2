package com.eme22.bolo.commands.admin;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class SendBirthDayMessageCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.forcebirthday", defaultValue = "")
   String[] aliases = new String[0];
   private final Bot bot;

   public SendBirthDayMessageCmd(@Named("adminCategory") Category category, Bot bot) {
      super(category);
      this.name = "forcebirthday";
      this.help = "envia un mensaje de cumpleaÃ±os al servidor";
      this.bot = bot;
   }

   public void execute(SlashCommandEvent event) {
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (s.getBirthdayChannelId() == 0L) {
         event.reply(event.getClient().getError() + " No hay un canal de cumpleaÃ±os especificado").setEphemeral(true).queue();
      } else {
         TextChannel channel = event.getGuild().getTextChannelById(s.getBirthdayChannelId());
         if (channel == null) {
            event.reply(event.getClient().getError() + " No se pudo encontrar el canal de cumpleaÃ±os").setEphemeral(true).queue();
         } else {
            this.bot.getBirthdayManager().remindBirthdays(this.bot, channel);
         }
      }
   }

   public void execute(CommandEvent event) {
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (s.getBirthdayChannelId() == 0L) {
         event.replyError(" No hay un canal de cumpleaÃ±os especificado");
      } else {
         TextChannel channel = event.getGuild().getTextChannelById(s.getBirthdayChannelId());
         if (channel == null) {
            event.replyError(" No se pudo encontrar el canal de cumpleaÃ±os");
         } else {
            this.bot.getBirthdayManager().remindBirthdays(this.bot, channel);
         }
      }
   }
}











