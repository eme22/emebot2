package com.eme22.bolo.commands.admin;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import jakarta.inject.Named;

import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.utils.FormatUtil;
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
public class SetWelcomeCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.sethello", defaultValue = "")
   String[] aliases = new String[0];

   public SetWelcomeCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "sethello";
      this.help = "especifica un canal para las bienvenidas";
      this.arguments = "<channel|NONE>";
      this.options = Collections.singletonList(
         new OptionData(OptionType.CHANNEL, "canal", "canal a poner para mensaje de bienvenidas. Se utilizara el canal por defecto si esta activado.")
            .setRequired(true)
      );
   }

   @Override
   public void execute(SlashCommandEvent event) {
      TextChannel channel = event.getOption("canal").getAsChannel().asTextChannel();
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      s.setBienvenidasChannelId(channel.getIdLong());
      s.persist();
      event.reply(event.getClient().getSuccess() + " El canal de las bienvenidas es ahora <#" + channel.getId() + ">").queue();
   }

   @Override
   public void execute(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.replyError(" Ponga un canal de texto o NONE");
      } else {
         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         if (event.getArgs().equalsIgnoreCase("none")) {
            s.setBienvenidasChannelId(0L);
            s.persist();
            event.replySuccess(" El canal de las bienvenidas ha sido quitado.");
         } else {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
               event.replyWarning(" No Text Channels found matching \"" + event.getArgs() + "\"");
            } else if (list.size() > 1) {
               event.replyWarning(FormatUtil.listOfTChannels(list, event.getArgs()));
            } else {
               s.setBienvenidasChannelId(list.get(0).getIdLong());
               s.persist();
               event.replySuccess(" El canal de las bienvenidas es ahora <#" + list.get(0).getId() + ">");
            }
         }
      }
   }
}








