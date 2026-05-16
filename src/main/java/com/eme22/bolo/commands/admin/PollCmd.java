package com.eme22.bolo.commands.admin;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class PollCmd extends AdminCommand {

   @ConfigProperty(name = "config.aliases.poll", defaultValue = "")
   String[] aliases = new String[0];

   public PollCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "poll";
      this.help = "crea una votacion con los datos enviados";
      this.arguments = "[Question] [Answer 1] [Answer 2]...[Answer 9]";
      this.options = Arrays.asList(
         new OptionData(OptionType.STRING, "pregunta", "pregunta a hacer").setRequired(true),
         new OptionData(OptionType.STRING, "respuesta1", "respuesta u opcion").setRequired(true),
         new OptionData(OptionType.STRING, "respuesta2", "respuesta u opcion").setRequired(true),
         new OptionData(OptionType.NUMBER, "duration", "duracion de la encuesta en minutos").setRequired(false),
         new OptionData(OptionType.STRING, "respuesta3", "respuesta u opcion adicional").setRequired(false),
         new OptionData(OptionType.STRING, "respuesta4", "respuesta u opcion adicional").setRequired(false),
         new OptionData(OptionType.STRING, "respuesta5", "respuesta u opcion adicional").setRequired(false),
         new OptionData(OptionType.STRING, "respuesta6", "respuesta u opcion adicional").setRequired(false),
         new OptionData(OptionType.STRING, "respuesta7", "respuesta u opcion adicional").setRequired(false),
         new OptionData(OptionType.STRING, "respuesta8", "respuesta u opcion adicional").setRequired(false),
         new OptionData(OptionType.STRING, "respuesta9", "respuesta u opcion adicional").setRequired(false)
      );
   }

   public void execute(SlashCommandEvent event) {
      OptionMapping questionOption = event.getOption("pregunta");
      MessagePollBuilder pollBuilder = new MessagePollBuilder(questionOption.getAsString());

      for (int i = 1; i < 10; i++) {
         OptionMapping responseOption = event.getOption("respuesta" + i);
         if (responseOption != null) {
            pollBuilder.addAnswer(responseOption.getAsString());
         }
      }

      if (event.getOption("duration") != null) {
         pollBuilder.setDuration(event.getOption("duration").getAsLong(), TimeUnit.MINUTES);
      }

      ((MessageCreateAction)event.getTextChannel().sendMessage("").setPoll(pollBuilder.build()))
         .queue(
            message -> event.reply(event.getClient().getSuccess() + " Encuesta creada con exito. Puedes cancelarla borrando el mensaje.")
               .setEphemeral(true)
               .queue()
         );
   }

   public void execute(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.reply(event.getClient().getError() + " Por favor incluya al menos una Pregunta y 2 respuestas en la pregunta");
      } else {
         MessagePollBuilder pollBuilder = null;
         Pattern p = Pattern.compile("\\[(.*?)\\]");
         Matcher m = p.matcher(event.getArgs());

         int[] i;
         for (i = new int[]{1}; m.find() && i[0] < 10; i[0]++) {
            if (i[0] == 1) {
               pollBuilder = new MessagePollBuilder(m.group(1));
            } else {
               pollBuilder.addAnswer(m.group(1));
            }
         }

         if (i[0] == 10) {
            i[0] = 9;
         }

         if (pollBuilder == null) {
            event.reply(event.getClient().getError() + " Por favor incluya al menos una Pregunta y 2 respuestas en la pregunta");
         } else {
            ((MessageCreateAction)event.getTextChannel().sendMessage("").setPoll(pollBuilder.build()))
               .queue(message -> event.replySuccess(event.getClient().getSuccess() + " Encuesta creada con exito. Puedes cancelarla borrando el mensaje."));
         }
      }
   }
}











