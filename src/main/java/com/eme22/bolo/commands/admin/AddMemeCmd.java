package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class AddMemeCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.addmeme", defaultValue = "")
   String[] aliases = new String[0];

   public AddMemeCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "addmeme";
      this.help = "agrega un meme para el comando especial de memes, puede ser adjuntado al mensaje";
      this.arguments = "<meme> <link>";
      this.options = Arrays.asList(
         new OptionData(OptionType.STRING, "meme", "nombre o descripcion del meme").setRequired(true),
         new OptionData(OptionType.STRING, "link", "link de la imagen del meme").setRequired(true)
      );
   }

   protected void execute(SlashCommandEvent event) {
      String message = Objects.requireNonNull(event.getOption("meme")).getAsString();
      String link = Objects.requireNonNull(event.getOption("link")).getAsString();

      try {
         java.net.URI.create(link).toURL();
      } catch (java.net.MalformedURLException | IllegalArgumentException var5) {
         event.reply(event.getClient().getError() + " Link Incorrecto").setEphemeral(true).queue();
         return;
      }

      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      s.addToMemeImages(message, link);
      s.persist();
      event.reply(event.getClient().getSuccess() + " Imagen " + link + " Agregada a la lista de memes").setEphemeral(true).queue();
   }

   protected void execute(CommandEvent event) {
      String link = null;
      if (event.getArgs().isEmpty()) {
         List<Attachment> attachmentList = event.getMessage().getAttachments();
         if (attachmentList.isEmpty()) {
            event.reply(event.getClient().getError() + " Incluya texto y un link");
            return;
         }

         link = attachmentList.get(0).getUrl();
      }

      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      String message;
      if (link != null) {
         message = event.getArgs();
      } else {
         String args = event.getArgs();
         message = args.substring(0, args.lastIndexOf(" "));
         link = args.substring(args.lastIndexOf(" ") + 1);
      }

      try {
         java.net.URI.create(link).toURL();
      } catch (java.net.MalformedURLException | IllegalArgumentException var6) {
         event.replyError(" Link Incorrecto");
         return;
      }

      s.addToMemeImages(message, link);
      s.persist();
      event.reply(event.getClient().getSuccess() + " Imagen " + link + " Agregada a la lista de memes");
   }
}



