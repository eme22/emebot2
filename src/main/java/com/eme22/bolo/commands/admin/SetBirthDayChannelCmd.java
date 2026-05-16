package com.eme22.bolo.commands.admin;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Named;

import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import java.util.Collections;
import java.util.List;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class SetBirthDayChannelCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.setbdaychan", defaultValue = "")
   String[] aliases = new String[0];

   public SetBirthDayChannelCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "setbdaychan";
      this.help = "especifica un canal para los cumpleaÃ±os";
      this.arguments = "<channel|NONE>";
      this.options = Collections.singletonList(
         new OptionData(OptionType.CHANNEL, "canal", "canal a poner para mensaje de cumpleaÃ±os. Se utilizara el canal por defecto si esta activado.")
            .setRequired(true)
      );
   }

   public void execute(SlashCommandEvent event) {
      TextChannel channel = event.getOption("canal").getAsChannel().asTextChannel();
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      s.setBirthdayChannelId(channel.getIdLong());
      s.persist();
      event.reply(event.getClient().getSuccess() + " El canal de los cumpleaÃ±os es ahora <#" + channel.getId() + ">").queue();
   }

   public void execute(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.replyError(" Ponga un canal de texto o NONE");
      } else if (event.getArgs().equalsIgnoreCase("NONE")) {
         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         s.setBirthdayChannelId(0L);
         s.persist();
         event.replySuccess(" El canal de los cumpleaÃ±os ha sido desactivado");
      } else {
         List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
         if (list.isEmpty()) {
            event.replyError(" No se encontraron canales de texto");
         } else {
            TextChannel channel = list.get(0);
            Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
            s.setBirthdayChannelId(channel.getIdLong());
            s.persist();
            event.replySuccess(" El canal de los cumpleaÃ±os es ahora <#" + channel.getId() + ">");
         }
      }
   }
}











