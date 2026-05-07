package com.eme22.bolo.listeners;

import com.eme22.bolo.stats.StatsService;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CommandListener;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.runtime.annotations.RegisterForReflection;

@ApplicationScoped
@RegisterForReflection(methods = true)
public class StatsListener implements CommandListener {
   private final StatsService statsService;

   @Inject
   public StatsListener(StatsService statsService) {
      this.statsService = statsService;
   }

   public void onCommand(CommandEvent event, Command command) {
      this.statsService.updateCommandsUsed(event.getGuild().getIdLong());
      CommandListener.super.onCommand(event, command);
   }

   public void onSlashCommand(SlashCommandEvent event, SlashCommand command) {
      this.statsService.updateCommandsUsed(event.getGuild().getIdLong());
      CommandListener.super.onSlashCommand(event, command);
   }
}
