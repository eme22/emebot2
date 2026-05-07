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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class SetGoodByeCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.setgoodbye", defaultValue = "")
   String[] aliases = new String[0];

   public SetGoodByeCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "setgoodbye";
      this.help = "especifica un canal para las despedidas";
      this.arguments = "<channel|NONE>";
      this.options = Collections.singletonList(
         new OptionData(OptionType.CHANNEL, "canal", "canal a poner para mensaje de despedidas. Se utilizara el canal por defecto si esta activado.")
            .setRequired(true)
      );
   }

   protected void execute(SlashCommandEvent event) {
      TextChannel channel = event.getOption("canal").getAsChannel().asTextChannel();
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      s.setDespedidasChannelId(channel.getIdLong());
      s.persist();
      event.reply(event.getClient().getSuccess() + " El canal de las despedidas es ahora <#" + channel.getId() + ">").queue();
   }

   protected void execute(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.reply(event.getClient().getError() + " Ponga un canal de texto o NONE");
      } else {
         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         if (event.getArgs().equalsIgnoreCase("none")) {
            s.setDespedidasChannelId(0L);
            s.persist();
            event.reply(event.getClient().getSuccess() + " El canal de las despedidas ha sido quitado.");
         } else {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
               event.reply(event.getClient().getWarning() + " No Text Channels found matching \"" + event.getArgs() + "\"");
            } else if (list.size() > 1) {
               event.reply(event.getClient().getWarning() + FormatUtil.listOfTChannels(list, event.getArgs()));
            } else {
               s.setDespedidasChannelId(list.get(0).getIdLong());
               s.persist();
               event.reply(event.getClient().getSuccess() + " El canal de las despedidas es ahora <#" + list.get(0).getId() + ">");
            }
         }
      }
   }
}



