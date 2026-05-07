package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.commands.AdminCommand;
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
public class BotMessageCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.message", defaultValue = "")
   String[] aliases = new String[0];

   public BotMessageCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "message";
      this.help = "hace hablar al bot";
      this.arguments = "<mensaje>";
      this.options = Collections.singletonList(new OptionData(OptionType.STRING, "mensaje", "mensaje a decir").setRequired(true));
   }

   protected void execute(SlashCommandEvent event) {
      String message = Objects.requireNonNull(event.getOption("mensaje")).getAsString();
      event.reply(event.getClient().getSuccess() + " Mensaje Enviado").setEphemeral(true).queue();
      event.getChannel().sendMessage(message).queue();
   }

   protected void execute(CommandEvent event) {
      String message = event.getArgs();
      if (message.isEmpty()) {
         event.replyError(" Incluya un mensaje");
      } else {
         event.reply(message);
      }
   }
}



