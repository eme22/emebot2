package com.eme22.bolo.commands.owner;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class ShutdownCmd extends OwnerCommand {
   @ConfigProperty(name = "config.aliases.shutdown", defaultValue = "")
   String[] aliases = new String[0];
   private final Bot bot;

   public ShutdownCmd(Bot bot) {
      this.bot = bot;
      this.name = "shutdown";
      this.help = "safely shuts down";
      this.guildOnly = false;
   }

   protected void execute(SlashCommandEvent event) {
      event.reply(event.getClient().getWarning() + " Apagando...").queue();
      this.bot.shutdown();
   }

   protected void execute(CommandEvent event) {
      event.replyWarning(" Apagando...");
      this.bot.shutdown();
   }
}


