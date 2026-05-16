package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.Collections;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class EightBallCmd extends BaseCommand {
   @ConfigProperty(name = "config.aliases.8ball", defaultValue = "")
   String[] aliases = new String[0];

   public EightBallCmd(Bot bot) {
      this.name = "8ball";
      this.arguments = "<pregunta>";
      this.help = "pregunta al azar a la bola de 8";
      this.guildOnly = true;
      this.options = Collections.singletonList(new OptionData(OptionType.STRING, "pregunta", "pregunta que vas a realizar").setRequired(true));
   }

   public void execute(SlashCommandEvent event) {
      Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
      String question = event.getOption("pregunta").getAsString();
      if (question.trim().isEmpty()) {
         event.reply("Â¡Escribe una pregunta!").complete();
      } else {
         EmbedBuilder response = new EmbedBuilder()
            .setTitle("PregÃºntale a " + event.getGuild().getSelfMember().getUser().getName())
            .setDescription("**" + question + "**\n> " + settings.getRandomAnswer());
         event.replyEmbeds(response.build(), new MessageEmbed[0]).queue();
      }
   }

   public void execute(CommandEvent event) {
      Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
      String question = event.getArgs();
      if (question.trim().isEmpty()) {
         event.reply("Â¡Escribe una pregunta!");
      } else {
         EmbedBuilder response = new EmbedBuilder()
            .setTitle("PregÃºntale a " + event.getGuild().getSelfMember().getUser().getName())
            .setDescription("**" + question + "**\n> " + settings.getRandomAnswer());
         event.reply(response.build());
      }
   }
}










