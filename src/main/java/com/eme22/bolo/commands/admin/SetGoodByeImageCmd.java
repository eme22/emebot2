package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.Collections;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class SetGoodByeImageCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.setgoodbyeimage", defaultValue = "")
   String[] aliases = new String[0];

   public SetGoodByeImageCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "setgoodbyeimg";
      this.help = "cambia la imagen de despedidas a una personalizada";
      this.arguments = "<link|NONE>";
      this.options = Collections.singletonList(new OptionData(OptionType.STRING, "imagen", "imagen de fondo del mensaje de despedidas.").setRequired(true));
   }

   protected void execute(SlashCommandEvent event) {
      String image = event.getOption("imagen").getAsString();
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (image.equalsIgnoreCase("none")) {
         s.setDespedidasChannelImage(null);
         s.persist();
         event.reply(event.getClient().getSuccess() + " La imagen de despedidas se ha quitado.").queue();
      } else {
         if (OtherUtil.checkImage(image)) {
            s.setDespedidasChannelImage(image);
            s.persist();
            event.reply(event.getClient().getSuccess() + "La imagen de despedidas es ahora " + image).queue();
         } else {
            event.reply(event.getClient().getError() + " Incluya un link a una imagen valida o NONE para usar la imagen por defecto")
               .setEphemeral(true)
               .queue();
         }
      }
   }

   protected void execute(CommandEvent event) {
      String image = event.getArgs();
      if (image.isEmpty()) {
         event.replyError(" Incluya un link a una imagen o NONE para usar la imagen por defecto");
      } else {
         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         if (image.equalsIgnoreCase("none")) {
            s.setDespedidasChannelImage(null);
            s.persist();
            event.replySuccess(" La imagen de despedidas se ha quitado.");
         } else {
            if (OtherUtil.checkImage(image)) {
               s.setDespedidasChannelImage(image);
               s.persist();
               event.replySuccess(" La imagen de despedidas es ahora " + image);
            } else {
               event.replyError(" Incluya un link a una imagen valida o NONE para usar la imagen por defecto");
            }
         }
      }
   }
}



