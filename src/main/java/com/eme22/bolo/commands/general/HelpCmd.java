package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Generated;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton @jakarta.inject.Named("HelpCmd")
@Slf4j
@Transactional
@ActivateRequestContext
public class HelpCmd extends BaseCommand {
   @Generated
   
   private final Bot bot;
   private static final String DEFAULT_PREFIX = "@mention";
   @ConfigProperty(name = "config.aliases.help", defaultValue = "")
   String[] aliases = new String[0];
   @ConfigProperty(name = "config.discordChannel")
   String botDiscord;

   public HelpCmd(Bot bot) {
      this.name = "help";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_US, "help", DiscordLocale.SPANISH, "ayuda", DiscordLocale.SPANISH_LATAM, "ayuda", DiscordLocale.ENGLISH_UK, "help"
      );
      this.help = "muestra la ayuda";
      this.guildOnly = true;
      this.bot = bot;
   }

   public void execute(SlashCommandEvent event) {
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      Server settings = this.bot.getSettingsManager().getSettings(event.getGuild());
      StringBuilder builder = new StringBuilder("Comandos de **" + event.getJDA().getSelfUser().getName() + "**\n");
      List<String> strings = new ArrayList<>();
      int preMaxSize = 1980;
      Category category = null;

      for (Command command : event.getClient().getCommands()) {
         if (!command.isHidden() && (!command.isOwnerCommand() || event.getUser().getId().equals(event.getClient().getOwnerId()))) {
            if (!Objects.equals(category, command.getCategory())) {
               category = command.getCategory();
               builder.append("\n\n  __").append(category == null ? "Miscelaneos" : category.getName()).append("__:\n");
            }

            builder.append("\n`")
               .append(settings.getPrefix().equals("@mention") ? "@" + event.getJDA().getSelfUser().getName() + " " : settings.getPrefix())
               .append(settings.getPrefix() == null ? " " : "")
               .append(command.getName())
               .append(command.getArguments() == null ? "`" : " " + command.getArguments() + "`")
               .append(" - ")
               .append(command.getHelp());
            if (builder.length() > preMaxSize) {
               strings.add(builder.toString());
               builder = new StringBuilder();
            }
         }
      }

      User owner2 = event.getJDA().getUserById(event.getClient().getOwnerId());
      log.error("Owner2: " + owner2);
      if (owner2 != null) {
         if (builder.length() > preMaxSize) {
            strings.add(builder.toString());
            builder = new StringBuilder();
         }

         builder.append("\n\nPara ayuda adicional, contacta a **").append(owner2.getName()).append("**#").append(owner2.getDiscriminator());
         if (this.botDiscord != null && !this.botDiscord.isEmpty()) {
            builder.append(" o unete a ").append(this.botDiscord);
         }

         strings.add(builder.toString());
      }

      for (String message : strings) {
         event.getUser()
            .openPrivateChannel()
            .queue(
               channel -> channel.sendMessage(message)
                  .queue(
                     success -> event.reply("La ayuda ha sido enviada a tus Mensajes Directos.").queue(),
                     error -> event.reply("La ayuda no puede ser enviada porque tienes los Mensajes Directos bloqueados.").queue()
                  )
            );
      }
   }

   public void execute(CommandEvent event) {
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      Server settings = this.bot.getSettingsManager().getSettings(event.getGuild());
      StringBuilder builder = new StringBuilder("Comandos de **" + event.getSelfUser().getName() + "**\n");
      List<String> strings = new ArrayList<>();
      int preMaxSize = 1980;
      Category category = null;

      for (Command command : event.getClient().getCommands()) {
         if (!command.isHidden() && (!command.isOwnerCommand() || event.isOwner())) {
            if (!Objects.equals(category, command.getCategory())) {
               category = command.getCategory();
               builder.append("\n\n  __").append(category == null ? "Miscelaneos" : category.getName()).append("__:\n");
            }

            builder.append("\n`")
               .append(settings.getPrefix().equals("@mention") ? "@" + event.getJDA().getSelfUser().getName() + " " : settings.getPrefix())
               .append(settings.getPrefix() == null ? " " : "")
               .append(command.getName())
               .append(command.getArguments() == null ? "`" : " " + command.getArguments() + "`")
               .append(" - ")
               .append(command.getHelp());
            if (builder.length() > preMaxSize) {
               strings.add(builder.toString());
               builder = new StringBuilder();
            }
         }
      }

      User owner2 = event.getJDA().getUserById(event.getClient().getOwnerId());
      log.error("Owner2: " + owner2);
      if (owner2 != null) {
         if (builder.length() > preMaxSize) {
            strings.add(builder.toString());
            builder = new StringBuilder();
         }

         builder.append("\n\nPara ayuda adicional, contacta a **").append(owner2.getName()).append("**#").append(owner2.getDiscriminator());
         if (this.botDiscord != null && !this.botDiscord.isEmpty()) {
            builder.append(" o unete a ").append(this.botDiscord);
         }

         strings.add(builder.toString());
      }

      for (String message : strings) {
         event.replyInDm(message, unused -> {}, t -> event.replyWarning("Help cannot be sent because you are blocking Direct Messages."));
      }
   }
}












