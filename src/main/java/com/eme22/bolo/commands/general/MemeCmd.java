package com.eme22.bolo.commands.general;

import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.model.MemeImage;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.stats.StatsService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.Collections;
import java.util.Objects;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class MemeCmd extends BaseCommand {
   private final StatsService statsService;

   @Inject
   public MemeCmd(@ConfigProperty(name = "config.aliases.meme", defaultValue = "") String[] aliases, StatsService statsService) {
      this.name = "meme";
      this.arguments = "NONE o <posicion>";
      this.help = "muestra un meme al azar del servidor";
      this.aliases = aliases;
      this.statsService = statsService;
      this.guildOnly = true;
      this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "posicion", "posicion del meme").setRequired(false));
   }

   protected void execute(SlashCommandEvent event) {
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      Integer pos = null;

      try {
         pos = Integer.parseInt(Objects.requireNonNull(event.getOption("posicion")).getAsString());
      } catch (NumberFormatException | NullPointerException var8) {
      }

      MemeImage data;
      try {
         if (pos != null) {
            data = s.getMemeImages().get(pos - 1);
         } else {
            data = s.getRandomMemeImages();
         }
      } catch (IndexOutOfBoundsException | IllegalArgumentException var7) {
         event.reply(event.getClient().getError() + "Meme invalido o no hay memes configurados en este servidor").queue();
         return;
      }

      MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
      EmbedBuilder eb = new EmbedBuilder().setImage(data.getMeme());
      messageCreateBuilder.addContent(data.getMessage());
      messageCreateBuilder.setEmbeds(new MessageEmbed[]{eb.build()});
      event.reply(messageCreateBuilder.build()).queue(success -> {
         this.statsService.updateImagesSend(event.getGuild().getIdLong());
         this.statsService.updateMemesSend(event.getGuild().getIdLong());
      });
   }

   protected void execute(CommandEvent event) {
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      Integer pos = null;

      try {
         pos = Integer.parseInt(event.getArgs());
      } catch (NumberFormatException var8) {
      }

      MemeImage data;
      try {
         if (pos != null) {
            data = s.getMemeImages().get(pos - 1);
         } else {
            data = s.getRandomMemeImages();
         }
      } catch (IndexOutOfBoundsException | IllegalArgumentException var7) {
         event.replyError("Meme invalido o no hay memes configurados en este servidor");
         return;
      }

      EmbedBuilder eb = new EmbedBuilder().setImage(data.getMeme());
      MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
      messageCreateBuilder.addContent(data.getMessage());
      messageCreateBuilder.setEmbeds(new MessageEmbed[]{eb.build()});
      event.reply(messageCreateBuilder.build(), success -> {
         this.statsService.updateImagesSend(event.getGuild().getIdLong());
         this.statsService.updateMemesSend(event.getGuild().getIdLong());
      });
      event.getMessage().delete().queue();
   }
}


