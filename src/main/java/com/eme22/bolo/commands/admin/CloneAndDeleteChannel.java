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
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class CloneAndDeleteChannel extends AdminCommand {
   @ConfigProperty(name = "config.aliases.clonechannel2", defaultValue = "")
   String[] aliases = new String[0];

   public CloneAndDeleteChannel(@Named("adminCategory") Category category) {
      super(category);
      this.name = "clonechannel2";
      this.help = "clona el canal especificado y lo borra";
      this.options = Collections.singletonList(new OptionData(OptionType.CHANNEL, "canal", "selecciona el canal a agregar.").setRequired(true));
   }

   public void execute(SlashCommandEvent event) {
      TextChannel channel = (TextChannel)event.optMessageChannel("canal");
      channel.createCopy().queue(success -> {
         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         this.checkAndDeleteChannel(channel, success, s, event.getGuild(), event.getJDA());
      });
   }

   public void execute(CommandEvent event) {
      TextChannel channel = event.getTextChannel();
      channel.createCopy().queue(success -> {
         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         this.checkAndDeleteChannel(channel, success, s, event.getGuild(), event.getJDA());
      });
   }

   private void checkAndDeleteChannel(TextChannel channel, TextChannel success, Server s, Guild guild, JDA jda) {
      if (s.getBienvenidasChannelId() == channel.getIdLong()) {
         s.setBienvenidasChannelId(success.getIdLong());
      }

      if (s.getDespedidasChannelId() == channel.getIdLong()) {
         s.setDespedidasChannelId(success.getIdLong());
      }

      if (s.getTextChannelId() == channel.getIdLong()) {
         s.setTextChannelId(success.getIdLong());
      }

      this.checkBienvenidas(guild, jda, success, s);
      channel.delete().queue();
      success.sendMessage("El canal se ha clonado con exito").queue();
   }

   private void checkBienvenidas(Guild guild, JDA jda, TextChannel success, Server s) {
      String bienvenidasMessage = s.getBienvenidasChannelMessage();
      List<TextChannel> channels = FinderUtil.findTextChannels(bienvenidasMessage, guild);
      List<Role> roles = FinderUtil.findRoles(bienvenidasMessage, guild);
      List<User> users = FinderUtil.findUsers(bienvenidasMessage, jda);
      if (!channels.isEmpty()) {
         bienvenidasMessage = bienvenidasMessage.replace(channels.get(0).getAsMention(), success.getAsMention());
         s.setBienvenidasChannelMessage(bienvenidasMessage);
      }

      if (!roles.isEmpty()) {
         bienvenidasMessage = bienvenidasMessage.replace(roles.get(0).getAsMention(), success.getAsMention());
         s.setBienvenidasChannelMessage(bienvenidasMessage);
      }

      if (!users.isEmpty()) {
         bienvenidasMessage = bienvenidasMessage.replace(users.get(0).getAsMention(), success.getAsMention());
         s.setBienvenidasChannelMessage(bienvenidasMessage);
      }
   }
}











