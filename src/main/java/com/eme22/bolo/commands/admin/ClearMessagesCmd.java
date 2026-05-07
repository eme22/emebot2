package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class ClearMessagesCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.clear", defaultValue = "")
   String[] aliases = new String[0];

   public ClearMessagesCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "clear";
      this.help = "limpia los mensajes especificados";
      this.arguments = "<1 - 50>";
      this.options = Collections.singletonList(
         new OptionData(OptionType.INTEGER, "mensajes", "numero entre 2 al 100").setMinValue(1L).setMaxValue(50L).setRequired(true)
      );
   }

   protected void execute(SlashCommandEvent event) {
      Integer values = (Integer)event.getOption("mensajes", OptionMapping::getAsInt);
      List<Message> messages = (List<Message>)event.getChannel().getHistory().retrievePast(values).complete();
      event.getTextChannel().purgeMessages(messages);
      event.reply(event.getClient().getSuccess() + " " + values + " mensajes borrados!").setEphemeral(true).queue();
   }

   protected void execute(CommandEvent event) {
      try {
         int values = Integer.parseInt(event.getArgs());
         if (values < 1 || values > 50) {
            event.replyError("El valor tiene que ser entre 1 y  100!");
            return;
         }

         List<Message> messages = (List<Message>)event.getChannel().getHistory().retrievePast(values + 1).complete();
         event.getTextChannel().purgeMessages(messages);
         event.getChannel()
            .sendMessage(event.getClient().getSuccess() + " " + values + " mensajes borrados!")
            .queue(m -> m.delete().queueAfter(5L, TimeUnit.SECONDS));
      } catch (NumberFormatException var4) {
         event.replyError("Escribe un numero entre 1 y 100");
      }
   }
}



