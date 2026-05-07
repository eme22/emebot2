package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.model.ServerStats;
import com.eme22.bolo.model.Stats;
import com.eme22.bolo.stats.StatsService;
import com.eme22.bolo.language.LanguageService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.awt.Color;
import java.util.List;
import java.util.Optional;

@Singleton
public class StatsCmd extends BaseCommand {

   private final StatsService statsService;
   private final Color color;
   private final Bot bot;

   @Inject
   public StatsCmd(Bot bot, StatsService statsService, Color color, @ConfigProperty(name = "config.aliases.stats", defaultValue = "estadisticas") String[] aliases) {
      this.name = "stats";
      this.help = "muestra las estadisticas del bot y del servidor";
      this.aliases = aliases;
      this.statsService = statsService;
      this.color = color;
      this.bot = bot;
   }

   @Override
   protected void execute(SlashCommandEvent event) {
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      event.replyEmbeds(buildStatsEmbed(event.getGuild().getIdLong(), event.getJDA().getSelfUser().getName(), languageService)).queue();
   }

   @Override
   protected void execute(CommandEvent event) {
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      event.reply(buildStatsEmbed(event.getGuild().getIdLong(), event.getSelfUser().getName(), languageService));
   }

   private MessageEmbed buildStatsEmbed(Long guildId, String botName, LanguageService languageService) {
      EmbedBuilder builder = new EmbedBuilder();
      builder.setColor(this.color);
      
      String title = languageService.getMessage("command.stats.title", botName);
      if (title.isEmpty()) title = "Estadisticas de " + botName;
      builder.setTitle(title);

      // Server Stats
      Optional<ServerStats> serverStatsOpt = statsService.getServerStat(guildId);
      StringBuilder serverDesc = new StringBuilder();
      if (serverStatsOpt.isPresent()) {
         ServerStats ss = serverStatsOpt.get();
         serverDesc.append(languageService.getMessage("command.stats.commands", ss.getCommandsUsed() != null ? ss.getCommandsUsed() : 0)).append("\n");
         serverDesc.append(languageService.getMessage("command.stats.songs", ss.getSongsPlayed() != null ? ss.getSongsPlayed() : 0)).append("\n");
         serverDesc.append(languageService.getMessage("command.stats.images", ss.getImagesSend() != null ? ss.getImagesSend() : 0)).append("\n");
         serverDesc.append(languageService.getMessage("command.stats.memes", ss.getMemesSend() != null ? ss.getMemesSend() : 0)).append("\n");
         serverDesc.append(languageService.getMessage("command.stats.kisses", ss.getKisses() != null ? ss.getKisses() : 0)).append("\n");
         serverDesc.append(languageService.getMessage("command.stats.slaps", ss.getSlaps() != null ? ss.getSlaps() : 0)).append("\n");
         serverDesc.append(languageService.getMessage("command.stats.pokes", ss.getPoke() != null ? ss.getPoke() : 0)).append("\n");
         serverDesc.append(languageService.getMessage("command.stats.bites", ss.getBite() != null ? ss.getBite() : 0)).append("\n");
         serverDesc.append(languageService.getMessage("command.stats.licks", ss.getLick() != null ? ss.getLick() : 0)).append("\n");
         serverDesc.append(languageService.getMessage("command.stats.fucks", ss.getFuck() != null ? ss.getFuck() : 0)).append("\n");
         serverDesc.append(languageService.getMessage("command.stats.cums", ss.getCum() != null ? ss.getCum() : 0)).append("\n");
         serverDesc.append(languageService.getMessage("command.stats.anals", ss.getAnal() != null ? ss.getAnal() : 0)).append("\n");
      } else {
         serverDesc.append("No hay datos para este servidor.");
      }

      String serverTitle = languageService.getMessage("command.stats.server");
      if (serverTitle.isEmpty()) serverTitle = "Estadisticas del Servidor";
      builder.addField(serverTitle, serverDesc.toString(), true);

      // Global Stats
      List<Stats> globalStats = statsService.getGlobalStats();
      StringBuilder globalDesc = new StringBuilder();
      if (globalStats != null && !globalStats.isEmpty()) {
         long commands = getStatValue(globalStats, "COMMANDS_USED");
         long songs = getStatValue(globalStats, "SONGS_PLAYED");
         long images = getStatValue(globalStats, "IMAGES_SEND");
         long memes = getStatValue(globalStats, "MEMES_SEND");
         long kisses = getStatValue(globalStats, "KISS");
         long slaps = getStatValue(globalStats, "SLAPS");
         long pokes = getStatValue(globalStats, "POKES");
         long bites = getStatValue(globalStats, "BITES");
         long licks = getStatValue(globalStats, "LICKS");
         long fucks = getStatValue(globalStats, "FUCKS");
         long cums = getStatValue(globalStats, "CUMS");
         long anals = getStatValue(globalStats, "ANAL");

         globalDesc.append(languageService.getMessage("command.stats.commands", commands)).append("\n");
         globalDesc.append(languageService.getMessage("command.stats.songs", songs)).append("\n");
         globalDesc.append(languageService.getMessage("command.stats.images", images)).append("\n");
         globalDesc.append(languageService.getMessage("command.stats.memes", memes)).append("\n");
         globalDesc.append(languageService.getMessage("command.stats.kisses", kisses)).append("\n");
         globalDesc.append(languageService.getMessage("command.stats.slaps", slaps)).append("\n");
         globalDesc.append(languageService.getMessage("command.stats.pokes", pokes)).append("\n");
         globalDesc.append(languageService.getMessage("command.stats.bites", bites)).append("\n");
         globalDesc.append(languageService.getMessage("command.stats.licks", licks)).append("\n");
         globalDesc.append(languageService.getMessage("command.stats.fucks", fucks)).append("\n");
         globalDesc.append(languageService.getMessage("command.stats.cums", cums)).append("\n");
         globalDesc.append(languageService.getMessage("command.stats.anals", anals)).append("\n");
      } else {
         globalDesc.append("No hay datos globales.");
      }

      String globalTitle = languageService.getMessage("command.stats.global");
      if (globalTitle.isEmpty()) globalTitle = "Estadisticas Globales";
      builder.addField(globalTitle, globalDesc.toString(), true);

      return builder.build();
   }

   private long getStatValue(List<Stats> statsList, String name) {
      for (Stats s : statsList) {
         if (s.getName().equals(name)) {
            return s.getValue() != null ? s.getValue() : 0;
         }
      }
      return 0;
   }
}
