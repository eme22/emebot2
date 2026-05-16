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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class AddEightBallAnswer extends AdminCommand {
   @ConfigProperty(name = "config.aliases.add8ballanswer", defaultValue = "")
   String[] aliases = new String[0];

   public AddEightBallAnswer(@Named("adminCategory") Category category) {
      super(category);
      this.name = "add8ballanswer";
      this.arguments = "<answer>";
      this.help = "agrega una respuesta a la bola de 8";
      this.options = Collections.singletonList(new OptionData(OptionType.STRING, "respuesta", "respuesta que vas a agregar").setRequired(true));
   }

   public void execute(SlashCommandEvent event) {
      Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
      String answer = event.getOption("respuesta").getAsString();
      settings.addToEightBallAnswers(answer);
      settings.persist();
      event.reply(event.getClient().getSuccess() + " **Respuesta agregada:** " + answer).queue();
   }

   public void execute(CommandEvent event) {
      Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
      String answer = event.getArgs();
      settings.addToEightBallAnswers(answer);
      settings.persist();
      event.replySuccess(" **Respuesta agregada:** " + answer);
   }
}











