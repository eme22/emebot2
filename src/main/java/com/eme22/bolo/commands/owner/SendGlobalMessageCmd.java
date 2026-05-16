package com.eme22.bolo.commands.owner;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.OwnerCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.Collections;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class SendGlobalMessageCmd extends OwnerCommand {
   @ConfigProperty(name = "config.aliases.sendom", defaultValue = "")
   String[] aliases = new String[0];
   Bot bot;

   public SendGlobalMessageCmd(Bot bot) {
      this.bot = bot;
      this.name = "sendom";
      this.help = "sends message from owner to the system channel of every server";
      this.arguments = "<message>";
      this.guildOnly = false;
      this.options = Collections.singletonList(new OptionData(OptionType.STRING, "message", "Mensaje a enviar").setRequired(true));
   }

   public void execute(SlashCommandEvent event) {
      String message = (String)event.getOption("message", OptionMapping::getAsString);
      if (message == null) {
         event.reply(event.getClient().getError() + " Mensaje Erroneo!!!").queue();
      } else {
         event.getJDA().getGuilds().forEach(guild -> {
            Server server = this.bot.getSettingsManager().getSettings(guild);
            guild.getTextChannelById(server.getTextChannelId()).sendMessage(message).queue();
         });
         event.reply("Mensaje Enviado!!!").queue();
      }
   }

   public void execute(CommandEvent event) {
      String message = event.getArgs();
      if (message == null) {
         event.replyError(" Mensaje Erroneo!!!");
      } else {
         event.getJDA().getGuilds().forEach(guild -> guild.getSystemChannel().sendMessage(message).queue());
      }
   }
}










