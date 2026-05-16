package com.eme22.bolo.commands.admin;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Named;

import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class CloneChannelCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.clonechannel", defaultValue = "")
   String[] aliases = new String[0];

   public CloneChannelCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "clonechannel";
      this.help = "clona el canal especificado";
   }

   public void execute(SlashCommandEvent event) {
      TextChannel channel = event.getTextChannel();
      channel.createCopy()
         .queue(
            success -> event.reply("El canal se ha clonado con exito").queue(),
            error -> event.reply(event.getClient().getError() + " El canal no se ha podido clonar").queue()
         );
   }

   public void execute(CommandEvent event) {
      TextChannel channel = event.getTextChannel();
      channel.createCopy()
         .queue(success -> event.replySuccess(" El canal se ha clonado con exito!!!"), error -> event.replyError("El canal no se ha podido clonar"));
   }
}











