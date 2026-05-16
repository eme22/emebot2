package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import com.eme22.bolo.commands.BaseCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class PingCmd extends BaseCommand {
   public PingCmd() {
      this.name = "ping";
      this.help = "checks the bot's latency";
      this.guildOnly = false;
      this.aliases = new String[]{"pong"};
   }

   public void execute(SlashCommandEvent event) {
      event.replyFormat(
            "Ping: %dms | Websocket: %dms",
            new Object[]{event.getHook().getInteraction().getTimeCreated().until(OffsetDateTime.now(), ChronoUnit.MILLIS), event.getJDA().getGatewayPing()}
         )
         .queue();
   }

   public void execute(CommandEvent event) {
      event.reply("Ping: ...", m -> {
         long ping = event.getMessage().getTimeCreated().until(m.getTimeCreated(), ChronoUnit.MILLIS);
         m.editMessage("Ping: " + ping + "ms | Websocket: " + event.getJDA().getGatewayPing() + "ms").queue();
      });
   }
}









