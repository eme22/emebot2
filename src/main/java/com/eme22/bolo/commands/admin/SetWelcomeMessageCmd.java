package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.Collections;
import java.util.Objects;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class SetWelcomeMessageCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.setwelcomemsg", defaultValue = "")
   String[] aliases = new String[0];

   public SetWelcomeMessageCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "setwelcomemsg";
      this.help = "cambia el mensaje de bienvenida";
      this.arguments = "<message>";
      this.options = Collections.singletonList(
         new OptionData(OptionType.STRING, "mensaje", "mensaje a decir cuando un usuario ingresa el servidor.").setRequired(true)
      );
   }

   protected void execute(SlashCommandEvent event) {
      String message = Objects.requireNonNull(event.getOption("mensaje")).getAsString();
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      s.setBienvenidasChannelMessage(message);
      s.persist();
      event.reply(event.getClient().getSuccess() + "El mensaje de bienvenida es ahora: \n\"" + message + "\"").queue();
   }

   protected void execute(CommandEvent event) {
      String image = event.getArgs();
      if (image.isEmpty()) {
         event.replyError(" Incluya un texto");
      } else {
         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         s.setBienvenidasChannelMessage(image);
         s.persist();
         event.replySuccess(" El mensaje de bienvenida es ahora: \n\"" + image + "\"");
      }
   }
}



