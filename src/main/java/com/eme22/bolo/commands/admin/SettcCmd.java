package com.eme22.bolo.commands.admin;

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
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class SettcCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.settc", defaultValue = "")
   String[] aliases = new String[0];

   public SettcCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "settc";
      this.help = "especifica un canal para los comandos de musica";
      this.arguments = "<channel|NONE>";
      this.options = Collections.singletonList(new OptionData(OptionType.CHANNEL, "canal", "canal a poner para solo comandos de musica.").setRequired(false));
   }

   protected void execute(SlashCommandEvent event) {
      OptionMapping option = event.getOption("canal");
      TextChannel channel = null;
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (option != null) {
         if (option.getAsChannel().getType().equals(ChannelType.TEXT)) {
            channel = option.getAsChannel().asTextChannel();
         }

         if (channel == null) {
            event.reply("Asegurese de que es un canal de texto").setEphemeral(true).queue();
         } else {
            s.setTextChannelId(channel.getIdLong());
            s.persist();
            event.reply(event.getClient().getSuccess() + " Music commands can now only be used in <#" + channel.getId() + ">").queue();
         }
      } else {
         s.setTextChannelId(0L);
         s.persist();
         event.reply(event.getClient().getSuccess() + " Los comandos de mÃºsica se pueden utilizar ahora en cualquier canal").queue();
      }
   }

   protected void execute(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.replyError(" Por favor, incluya un canal de texto o NONE");
      } else {
         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         if (event.getArgs().equalsIgnoreCase("none")) {
            s.setTextChannelId(0L);
            s.persist();
            event.replySuccess(" Los comandos de mÃºsica se pueden utilizar ahora en cualquier canal");
         } else {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
               event.replyWarning(" No Text Channels found matching \"" + event.getArgs() + "\"");
            } else if (list.size() > 1) {
               event.replyWarning(FormatUtil.listOfTChannels(list, event.getArgs()));
            } else {
               s.setTextChannelId(list.get(0).getIdLong());
               s.persist();
               event.replySuccess(" Los comandos de mÃºsica ahora sÃ³lo se pueden utilizar en <#" + list.get(0).getId() + ">");
            }
         }
      }
   }
}



