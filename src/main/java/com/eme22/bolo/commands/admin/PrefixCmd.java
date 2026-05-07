package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.Bot;
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
public class PrefixCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.prefix", defaultValue = "")
   String[] aliases = new String[0];

   public PrefixCmd(Bot bot, @Named("adminCategory") Category category) {
      super(category);
      this.name = "prefix";
      this.help = "pone un prefijo por servidor";
      this.arguments = "<prefix|NONE>";
      this.options = Collections.singletonList(
         new OptionData(OptionType.STRING, "prefix", "Selecciona el prefijo de los comandos (none = limpiar prefijo).").setRequired(true)
      );
   }

   protected void execute(SlashCommandEvent event) {
      String prefix = event.optString("prefix");
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (prefix != null && !prefix.equalsIgnoreCase("none")) {
         s.setPrefix(prefix);
         event.reply(event.getClient().getSuccess() + " Prefijo personalizado fijado en `" + prefix + "` en *" + event.getGuild().getName() + "*").queue();
      } else {
         s.setPrefix(null);
         event.reply(event.getClient().getSuccess() + " Prefijo del servidor limpiado.").queue();
      }

      s.persist();
   }

   protected void execute(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.replyError("Please include a prefix or NONE");
      } else {
         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         if (event.getArgs().equalsIgnoreCase("none")) {
            s.setPrefix(null);
            event.replySuccess("Prefijo del servidor limpiado.");
         } else {
            s.setPrefix(event.getArgs());
            event.replySuccess("Prefijo personalizado fijado en `" + event.getArgs() + "` en *" + event.getGuild().getName() + "*");
         }

         s.persist();
      }
   }
}



