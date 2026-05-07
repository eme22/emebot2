package com.eme22.bolo.commands.dj;

import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.commands.DJCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.Collections;
import java.util.Map;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class SkiptoCmd extends DJCommand {
   @ConfigProperty(name = "config.aliases.skipto", defaultValue = "")
   String[] aliases = new String[0];

   public SkiptoCmd(Bot bot, @Named("djCategory") Category category) {
      super(bot, category);
      this.name = "skipto";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "skipto",
         DiscordLocale.ENGLISH_US,
         "skipto",
         DiscordLocale.SPANISH,
         "saltarhasta",
         DiscordLocale.SPANISH_LATAM,
         "saltarhasta"
      );
      this.help = "skips to the specified song";
      this.descriptionLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "skips to the specified song",
         DiscordLocale.ENGLISH_US,
         "skips to the specified song",
         DiscordLocale.SPANISH,
         "salta a la canciÃ³n especificada",
         DiscordLocale.SPANISH_LATAM,
         "salta a la canciÃ³n especificada"
      );
      this.arguments = "<posicion>";
      this.bePlaying = true;
      this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "posicion", "Posicion para cambiar a la cola").setRequired(true));
   }

   @Override
   public void doCommand(CommandEvent event) {
      int index = 0;

      try {
         index = Integer.parseInt(event.getArgs());
      } catch (NumberFormatException var4) {
         event.reply(event.getClient().getError() + " `" + event.getArgs() + "` is not a valid integer!");
         return;
      }

      AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      if (index >= 1 && index <= handler.getQueueManager().getQueue().size()) {
         handler.getQueueManager().getQueue().skip(index - 1);
         event.reply(event.getClient().getSuccess() + " Skipped to **" + handler.getQueueManager().getQueue().get(0).getTrack().getInfo().getTitle() + "**");
         handler.getAudioPlayer().get().stopTrack();
      } else {
         event.reply(event.getClient().getError() + " Position must be a valid integer between 1 and " + handler.getQueueManager().getQueue().size() + "!");
      }
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
      OptionMapping option = event.getOption("posicion");
      if (option != null) {
         int index = 0;

         try {
            index = Integer.parseInt(option.getAsString());
         } catch (NumberFormatException var5) {
            event.reply(event.getClient().getError() + " `" + option.getAsString() + "` is not a valid integer!").queue();
            return;
         }

         AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
         if (index >= 1 && index <= handler.getQueueManager().getQueue().size()) {
            handler.getQueueManager().getQueue().skip(index - 1);
            event.reply(event.getClient().getSuccess() + " Skipped to **" + handler.getQueueManager().getQueue().get(0).getTrack().getInfo().getTitle() + "**")
               .queue();
            handler.getAudioPlayer().get().stopTrack();
         } else {
            event.reply(event.getClient().getError() + " Position must be a valid integer between 1 and " + handler.getQueueManager().getQueue().size() + "!")
               .queue();
         }
      }
   }
}



