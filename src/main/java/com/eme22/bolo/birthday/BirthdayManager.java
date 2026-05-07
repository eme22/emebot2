package com.eme22.bolo.birthday;

import jakarta.inject.Inject;

import jakarta.enterprise.context.ApplicationScoped;

import com.eme22.bolo.Bot;
import com.eme22.bolo.model.Birthday;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.repository.BirthdayRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
@ApplicationScoped
public class BirthdayManager {
   private final BirthdayRepository birthDayRepository;
   private final Map<Long, ArrayList<Birthday>> scheduledBirthDays = new HashMap<>();
   private final String defaultBirthDayHeader = "¡Hola a todos los cumpleañeros de hoy!\n\nEn nombre de todo el equipo de @server, queremos desearles un cumpleaños lleno de alegría y momentos inolvidables. \ud83c\udf89\n\nCumpleañeros de hoy:\n";
   private final String defaultBirthDayFooter = "\nEsperamos que este nuevo año les traiga muchas bendiciones, éxitos y que todos sus sueños se hagan realidad. ¡Disfruten su día al máximo!\n\nCon cariño,\nEl equipo de @server";

   @Inject
   public BirthdayManager(BirthdayRepository repository) {
      this.birthDayRepository = repository;
   }

   public void remindBirthdays(@NotNull Bot bot) {
      LocalDate today = LocalDate.now();
      List<Birthday> birthdays = this.birthDayRepository.findByDate(today.getDayOfMonth(), today.getMonthValue());
      this.scheduledBirthDays.clear();
      birthdays.forEach(birthday -> this.scheduledBirthDays.computeIfAbsent(birthday.getServer(), k -> new ArrayList<>(Collections.singletonList(birthday))));
      this.scheduledBirthDays.forEach((guildId, guildBirthdays) -> {
         Guild guild = bot.getJDA().getGuildById(guildId);
         if (guild != null) {
            this.sendBirthdayReminders(guild, guildBirthdays, bot);
         }
      });
   }

   public void remindBirthdays(@NotNull Bot bot, Guild guild) {
      LocalDate today = LocalDate.now();
      List<Birthday> birthdays = this.birthDayRepository.findByDateAndServer(today.getDayOfMonth(), today.getMonthValue(), guild.getIdLong());
      this.sendBirthdayReminders(guild, birthdays, bot);
   }

   public void remindBirthdays(@NotNull Bot bot, TextChannel channel) {
      LocalDate today = LocalDate.now();
      List<Birthday> birthdays = this.birthDayRepository.findByDateAndServer(today.getDayOfMonth(), today.getMonthValue(), channel.getGuild().getIdLong());
      this.sendBirthdayReminders(channel, channel.getGuild(), birthdays, bot);
   }

   private void sendBirthdayReminders(Guild guild, List<Birthday> birthdays, Bot bot) {
      Server settings = bot.getSettingsManager().getSettings(guild);
      if (settings != null && settings.getBirthdayChannelId() != 0L) {
         TextChannel channel = guild.getTextChannelById(settings.getBirthdayChannelId());
         if (channel != null) {
            String message = this.buildBirthdayMessage(guild, birthdays, settings);
            this.sendBirthdayMessage(channel, message);
         }
      }
   }

   private void sendBirthdayReminders(TextChannel channel, Guild guild, List<Birthday> birthdays, Bot bot) {
      Server settings = bot.getSettingsManager().getSettings(guild);
      if (settings != null && settings.getBirthdayChannelId() != 0L) {
         String message = this.buildBirthdayMessage(guild, birthdays, settings);
         this.sendBirthdayMessage(channel, message);
      }
   }

   private String buildBirthdayMessage(Guild guild, List<Birthday> birthdays, Server server) {
      String header = this.parseString(
         server.getBirthdayTemplateHeader() == null
            ? "¡Hola a todos los cumpleañeros de hoy!\n\nEn nombre de todo el equipo de @server, queremos desearles un cumpleaños lleno de alegría y momentos inolvidables. \ud83c\udf89\n\nCumpleañeros de hoy:\n"
            : server.getBirthdayTemplateHeader(),
         guild
      );
      String footer = this.parseString(
         server.getBirthdayTemplateFooter() == null
            ? "\nEsperamos que este nuevo año les traiga muchas bendiciones, éxitos y que todos sus sueños se hagan realidad. ¡Disfruten su día al máximo!\n\nCon cariño,\nEl equipo de @server"
            : server.getBirthdayTemplateFooter(),
         guild
      );
      String birthdayMessages = birthdays.stream()
         .map(birthday -> "- " + guild.getMemberById(birthday.getUser()).getAsMention() + ": " + birthday.getMessage())
         .collect(Collectors.joining("\n"));
      return header + birthdayMessages + "\n" + footer;
   }

   private void sendBirthdayMessage(TextChannel channel, String birthday) {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setDescription(birthday);
      channel.sendMessageEmbeds(embedBuilder.build(), new MessageEmbed[0]).queue();
   }

   private String parseString(String defaultBirthDayHeader, Guild guild) {
      return defaultBirthDayHeader.replace("@server", guild.getName());
   }

   public MessageEmbed getBirthdaysToday(Guild guild) {
      LocalDate today = LocalDate.now();
      List<Birthday> birthdays = this.birthDayRepository.findByDate(today.getDayOfMonth(), today.getMonthValue());
      return this.getBirthdaysEmbed(guild, birthdays);
   }

   public MessageEmbed getBirthdays(Guild guild) {
      List<Birthday> birthdays = this.birthDayRepository.findByServer(guild.getIdLong());
      return this.getBirthdaysEmbed(guild, birthdays);
   }

   private MessageEmbed getBirthdaysEmbed(Guild guild, List<Birthday> birthdays) {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setTitle("Cumpleaños de " + guild.getName());
      embedBuilder.setDescription(
         birthdays.stream()
            .map(
               birthday -> guild.getMemberById(birthday.getUser()).getAsMention()
                  + ": "
                  + birthday.getMessage()
                  + " - "
                  + birthday.getDate()
                  + " Activado: "
                  + birthday.isEnabled()
            )
            .collect(Collectors.joining("\n"))
      );
      return embedBuilder.build();
   }
}
