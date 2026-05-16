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
public class DeleteEightBallAnswer extends AdminCommand {
   @ConfigProperty(name = "config.aliases.del8ballanswer", defaultValue = "")
   String[] aliases = new String[0];

   public DeleteEightBallAnswer(@Named("adminCategory") Category category) {
      super(category);
      this.name = "del8ballanswer";
      this.arguments = "<answer>";
      this.help = "elimina una respuesta a la bola de 8";
      this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "posicion", "respuesta que vas a borrar").setRequired(true));
   }

   public void execute(SlashCommandEvent event) {
      Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
      Integer answer = (Integer)event.getOption("posicion", OptionMapping::getAsInt);
      if (answer != null && answer < settings.getEightBallAnswers().size()) {
         settings.removeFrom8BallAnswers(answer);
         settings.persist();
         event.reply(event.getClient().getSuccess() + " **Respuesta eliminada en la posicion: " + answer + " **").queue();
      } else {
         event.reply(event.getClient().getError() + " Posicion incorrecta!!").queue();
      }
   }

   public void execute(CommandEvent event) {
      Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
      int answer = Integer.parseInt(event.getArgs());
      if (answer >= settings.getEightBallAnswers().size()) {
         event.replyError(" Posicion incorrecta!!");
      } else {
         settings.removeFrom8BallAnswers(answer);
         settings.persist();
         event.replySuccess(" **Respuesta eliminada en la posicion: " + answer + " **");
      }
   }
}











