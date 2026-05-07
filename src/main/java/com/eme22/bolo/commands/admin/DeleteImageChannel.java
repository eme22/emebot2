package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import java.util.Collections;
import java.util.List;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class DeleteImageChannel extends AdminCommand {
   @ConfigProperty(name = "config.aliases.delimagechannel", defaultValue = "")
   String[] aliases = new String[0];

   public DeleteImageChannel(@Named("adminCategory") Category category) {
      super(category);
      this.name = "delimagechannel";
      this.help = "elimina un canal de la lista de no texto";
      this.arguments = "<channel>";
      this.options = Collections.singletonList(new OptionData(OptionType.CHANNEL, "canal", "selecciona el canal a quitar.").setRequired(true));
   }

   protected void execute(SlashCommandEvent event) {
      TextChannel textChannel = event.getOption("canal").getAsChannel().asTextChannel();
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (s.isOnlyImageChannel(textChannel)) {
         s.removeFromOnlyImageChannels(textChannel);
         s.persist();
         event.reply(event.getClient().getSuccess() + " Canal <#" + textChannel.getId() + "> quitado de la lista de canales sin texto").queue();
      } else {
         event.reply(event.getClient().getError() + " Canal <#" + textChannel.getId() + "> no esta en la lista de canales sin texto")
            .setEphemeral(true)
            .queue();
      }
   }

   protected void execute(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.replyError(" Incluya un canal de Texto");
      } else {
         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
         if (list.isEmpty()) {
            event.replyWarning(" No se han encontrado canales de texto que coincidan con \"" + event.getArgs() + "\"");
         } else {
            list.forEach(textChannel -> {
               if (s.isOnlyImageChannel(textChannel)) {
                  s.removeFromOnlyImageChannels(textChannel);
                  s.persist();
                  event.replySuccess(" Canal <#" + textChannel.getId() + "> quitado de la lista de canales sin texto");
               } else {
                  event.replyError(" Canal <#" + textChannel.getId() + "> no esta en la lista de canales sin texto");
               }
            });
         }
      }
   }
}



