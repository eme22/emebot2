package com.eme22.bolo.commands.admin;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

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
@Transactional
@ActivateRequestContext
public class SetWelcomeEnabledCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.sethelloenabled", defaultValue = "")
   String[] aliases = new String[0];

   public SetWelcomeEnabledCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "sethelloon";
      this.help = "activa o desactiva los mensajes de bienvenida";
      this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, "estado", "activa o desactiva los mensajes de bienvenida.").setRequired(true));
      this.arguments = "<true - false>";
   }

   @Override
   public void execute(SlashCommandEvent event) {
      OptionMapping canal = event.getOption("estado");
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (canal != null && canal.getAsBoolean()) {
         event.reply("El mensaje de bienvenida se ha activado").queue();
         s.setBienvenidasChannelEnabled(true);
      } else {
         event.reply("El mensaje de bienvenida se ha desactivado").queue();
         s.setBienvenidasChannelEnabled(false);
      }

      s.persist();
   }

   @Override
   public void execute(CommandEvent event) {
      String estado = event.getArgs();
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (estado.equals("true")) {
         event.replySuccess(" El mensaje de bienvenida se ha activado");
         s.setBienvenidasChannelEnabled(true);
         s.persist();
      } else if (estado.equals("false")) {
         event.replySuccess(" El mensaje de bienvenida se ha desactivado");
         s.setBienvenidasChannelEnabled(false);
         s.persist();
      } else if (!s.getBienvenidasChannelEnabled()) {
         event.replySuccess(" El mensaje de bienvenida se ha activado");
         s.setBienvenidasChannelEnabled(true);
         s.persist();
      } else {
         event.replySuccess(" El mensaje de bienvenida se ha desactivado");
         s.setBienvenidasChannelEnabled(false);
         s.persist();
      }
   }
}








