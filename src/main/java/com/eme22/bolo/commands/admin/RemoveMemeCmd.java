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
import java.util.Objects;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class RemoveMemeCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.delmeme", defaultValue = "")
   String[] aliases = new String[0];

   public RemoveMemeCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "delmeme";
      this.help = "borra un meme de la lista de memes";
      this.arguments = "<posicion>";
      this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "posicion", "posicion en la que esta el meme a borrar").setRequired(true));
   }

   public void execute(SlashCommandEvent event) {
      int a = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(event.getOption("posicion")).getAsString()));
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());

      try {
         s.deleteFromMemeImages(a - 1);
         s.persist();
      } catch (IndexOutOfBoundsException var5) {
         event.reply(event.getClient().getError() + " Numero incorrecto").setEphemeral(true).queue();
         return;
      }

      event.reply(event.getClient().getSuccess() + " Imagen " + a + " borrada de la lista de memes").queue();
   }

   public void execute(CommandEvent event) {
      String args = event.getArgs();
      if (args.isEmpty()) {
         event.reply(event.getClient().getError() + " Incluya un numero");
      } else {
         int a;
         try {
            a = Integer.parseInt(args);
         } catch (NumberFormatException var7) {
            event.replyError(" Incluya un numero");
            return;
         }

         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());

         try {
            s.deleteFromMemeImages(a - 1);
            s.persist();
         } catch (IndexOutOfBoundsException var6) {
            event.replyError("Numero incorrecto");
            return;
         }

         event.replySuccess(" Imagen " + a + " borrada de la lista de memes");
      }
   }
}











