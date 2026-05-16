package com.eme22.bolo.commands.admin;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
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
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class AddImageChannel extends AdminCommand {
   @ConfigProperty(name = "config.aliases.addimgch", defaultValue = "")
   String[] aliases = new String[0];

   public AddImageChannel(@Named("adminCategory") Category category) {
      super(category);
      this.name = "addimgch";
      this.help = "agrega un canal a la lista de no texto";
      this.arguments = "<channel>";
      this.options = Collections.singletonList(new OptionData(OptionType.CHANNEL, "canal", "selecciona el canal a agregar.").setRequired(true));
   }

   public void execute(SlashCommandEvent event) {
      OptionMapping canal = event.getOption("canal");
      TextChannel textChannel = null;
      if (canal != null) {
         textChannel = canal.getAsChannel().asTextChannel();
      }

      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (textChannel != null) {
         s.addOnlyImageChannels(textChannel.getIdLong());
         s.persist();
         event.reply(event.getClient().getSuccess() + " Canal <#" + textChannel.getId() + "> Agregado a la lista de canales sin texto")
            .setEphemeral(true)
            .queue();
      } else {
         event.reply(event.getClient().getError() + " Asegurese de que es un canal de texto").setEphemeral(true).queue();
      }
   }

   public void execute(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.replyError(" Incluya un canal de Texto");
      } else {
         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
         if (list.isEmpty()) {
            event.replyError(" No se han encontrado canales de texto que coincidan con: \"" + event.getArgs() + "\"");
         } else {
            list.forEach(textChannel -> {
               s.addOnlyImageChannels(textChannel.getIdLong());
               s.persist();
               event.reply(event.getClient().getSuccess() + " Canal <#" + textChannel.getId() + "> Agregado a la lista de canales sin texto");
            });
         }
      }
   }
}











