package com.eme22.bolo.commands.admin;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Named;

import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.utils.FormatUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import java.util.Collections;
import java.util.List;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class SetvcCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.setvc", defaultValue = "")
   String[] aliases = new String[0];

   public SetvcCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "setvc";
      this.help = "especifica un canal para la musica";
      this.arguments = "<channel|NONE>";
      this.options = Collections.singletonList(new OptionData(OptionType.CHANNEL, "canal", "canal a poner para especificar canal de voz.").setRequired(true));
   }

   public void execute(SlashCommandEvent event) {
      OptionMapping option = event.getOption("canal");
      VoiceChannel channel = null;
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (option != null) {
         if (option.getAsChannel().getType().equals(ChannelType.VOICE)) {
            channel = option.getAsChannel().asVoiceChannel();
         }

         if (channel != null) {
            s.setVoiceChannelId(channel.getIdLong());
            s.persist();
            event.reply(event.getClient().getSuccess() + " Ahora la mÃºsica sÃ³lo puede reproducirse en " + channel.getAsMention()).queue();
         } else {
            event.reply("Asegurese de que es un canal de voz").setEphemeral(true).queue();
         }
      } else {
         s.setTextChannelId(0L);
         s.persist();
         event.reply(event.getClient().getSuccess() + " Los comandos de mÃºsica se pueden utilizar ahora en cualquier canal").queue();
      }
   }

   public void execute(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.reply(event.getClient().getError() + " Por favor, incluya un canal de voz o NONE para ninguno");
      } else {
         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         if (event.getArgs().equalsIgnoreCase("none")) {
            s.setVoiceChannelId(0L);
            s.persist();
            event.reply(event.getClient().getSuccess() + " Ahora se puede reproducir mÃºsica en cualquier canal");
         } else {
            List<VoiceChannel> list = FinderUtil.findVoiceChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
               event.reply(event.getClient().getWarning() + " No Voice Channels found matching \"" + event.getArgs() + "\"");
            } else if (list.size() > 1) {
               event.reply(event.getClient().getWarning() + FormatUtil.listOfVChannels(list, event.getArgs()));
            } else {
               s.setVoiceChannelId(list.get(0).getIdLong());
               s.persist();
               event.reply(event.getClient().getSuccess() + " Ahora la mÃºsica sÃ³lo puede reproducirse en " + list.get(0).getAsMention());
            }
         }
      }
   }
}











