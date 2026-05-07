package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.Collections;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class SetGoodByeEnabledCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.setgoodbyeenabled", defaultValue = "")
   String[] aliases = new String[0];

   public SetGoodByeEnabledCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "setgoodbyeon";
      this.help = "activa o desactiva los mensajes de despedida";
      this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, "estado", "activa o desactiva los mensajes de despedida.").setRequired(true));
      this.arguments = "<true - false>";
   }

   protected void execute(SlashCommandEvent event) {
      OptionMapping canal = event.getOption("estado");
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (canal != null && canal.getAsBoolean()) {
         s.setDespedidasChannelEnabled(true);
         event.reply(event.getClient().getSuccess() + " El mensaje de despedida se ha activado").queue();
      } else {
         s.setDespedidasChannelEnabled(false);
         event.reply(event.getClient().getSuccess() + " El mensaje de despedida se ha desactivado").queue();
      }

      s.persist();
   }

   protected void execute(CommandEvent event) {
      String estado = event.getArgs();
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (estado.equals("true")) {
         event.replySuccess(" El mensaje de despedida se ha activado");
         s.setDespedidasChannelEnabled(true);
         s.persist();
      } else if (estado.equals("false")) {
         event.replySuccess(" El mensaje de despedida se ha desactivado");
         s.setDespedidasChannelEnabled(false);
         s.persist();
      } else if (s.getDespedidasChannelEnabled()) {
         event.replySuccess(" El mensaje de despedida se ha desactivado");
         s.setDespedidasChannelEnabled(false);
         s.persist();
      } else {
         event.replySuccess(" El mensaje de despedida se ha activado");
         s.setDespedidasChannelEnabled(true);
         s.persist();
      }
   }
}



