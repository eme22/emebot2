package com.eme22.bolo.listeners;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.Bot;
import com.eme22.bolo.model.CommandLog;
import com.eme22.bolo.repository.CommandLogRepository;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CommandListener;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.eme22.bolo.stats.StatsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.Generated;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.enterprise.context.control.ActivateRequestContext;



@ApplicationScoped
@Slf4j
public class CommandLogListener implements CommandListener {
   
   private final CommandLogRepository repository;
   private final Bot bot;
   private final StatsService statsService;
   private static String clientName;

   @Inject
   public CommandLogListener(CommandLogRepository repository, Bot bot, StatsService statsService) {
      this.repository = repository;
      this.bot = bot;
      this.statsService = statsService;
   }

   @ActivateRequestContext
   @Transactional
   public void onCommand(CommandEvent event, Command command) {
      log.info(
         "Command: {} Arguments: {} User: {} Client: {} Server: {} Channel: {} Time: {}",
         command.getName(),
         event.getArgs(),
         event.getAuthor().getName(),
         this.getClientName(),
         event.getGuild().getName(),
         event.getChannel().getName(),
         LocalDate.now()
      );
      if (event.getGuild() != null) {
         this.statsService.increment(event.getGuild().getIdLong(), "COMMANDS_USED");
      }
      this.repository
         .persist(CommandLog.builder()
                 .command(command.getName())
                 .arguments(event.getArgs())
                 .user(event.getAuthor().getName())
                 .client(this.getClientName())
                 .server(event.getGuild().getName())
                 .channel(event.getChannel().getName())
                 .time(LocalDateTime.now())
                 .build()
         );
   }

   @ActivateRequestContext
   @Transactional
   public void onSlashCommand(SlashCommandEvent event, SlashCommand command) {
      log.info(
         "Command: {} Arguments: {} User: {} Client {} Server: {} Channel: {} Time: {}",
         command.getName(),
         Arrays.toString(event.getOptions().toArray()),
         event.getUser().getName(),
         this.getClientName(),
         event.getGuild().getName(),
         event.getChannel().getName(),
         LocalDate.now()
      );
      if (event.getGuild() != null) {
         this.statsService.increment(event.getGuild().getIdLong(), "COMMANDS_USED");
      }
      this.repository
         .persist(CommandLog.builder()
                 .command(command.getName())
                 .arguments(this.optionToString(event.getOptions()))
                 .user(event.getUser().getName())
                 .client(this.getClientName())
                 .server(event.getGuild().getName())
                 .channel(event.getChannel().getName())
                 .time(LocalDateTime.now())
                 .build()
         );
   }

   private String optionToString(List<OptionMapping> optionData) {
      return optionData.stream().map(option -> option.getName() + ": " + option.getAsString()).reduce("", (a, b) -> a + b + " ");
   }

   private String getClientName() {
      if (clientName == null) {
         clientName = this.bot.getJDA().getSelfUser().getName();
      }

      return clientName;
   }
}
