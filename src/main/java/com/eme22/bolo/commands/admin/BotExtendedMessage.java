package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.language.LanguageService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class BotExtendedMessage extends AdminCommand {
   private static final String DATE_PATTERN = "dd/MM HH:mm:ss";
   private static final String TIME_PATTERN = "hh.mm a";
   private static final String INTERVAL_PATTERN = "(\\d?\\d?\\d?\\d)([DHMS])";
   private static final String MESSAGE_PATTERN = "\\[(.*?)\\] \\[(.*?)\\] \\[(.*?)\\] \\[(.*?)\\]";
   private static final String NONE_PATTERN = "\\[NONE\\] \\[(.*?)\\]";
   @ConfigProperty(name = "config.aliases.messagext", defaultValue = "")
   String[] aliases = new String[0];
   private final Bot bot;

   public BotExtendedMessage(@Named("adminCategory") Category category, Bot bot) {
      super(category);
      this.name = "messagext";
      this.help = "hace hablar al bot con opciones extendidas";
      this.arguments = "[intervalo: 30S = 30 segundos, 1H = 1 hora, 2D = 2 dias, 1 vez = NONE ] [fecha de inicio dd/MM hh:mm:ss] [fecha de fin dd/MM hh:mm:ss] [mensaje (Comandos especiales %day% %month% %date% %time% %who%)]";
      this.guildOnly = true;
      this.bot = bot;
      this.options = Arrays.asList(
         new OptionData(OptionType.STRING, "intervalo", "intervalo: Ejemplo: 10S: S (Segundo) | M (Minuto) | H (Hora) | D (Dia) ").setRequired(true),
         new OptionData(OptionType.STRING, "inicio", "fecha de inicio dd/MM hh:mm:ss").setRequired(true),
         new OptionData(OptionType.STRING, "fin", "fecha de fin dd/MM hh:mm:ss").setRequired(true),
         new OptionData(OptionType.STRING, "message", "mensaje a decir (Comandos especiales %day% %month% %date% %time% %who% )").setRequired(true)
      );
   }

   protected void execute(SlashCommandEvent event) {
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      OptionMapping intervalo = event.getOption("intervalo");
      OptionMapping inicio = event.getOption("inicio");
      OptionMapping fin = event.getOption("fin");
      OptionMapping message = event.getOption("message");
      long interval = this.parseInterval(intervalo.getAsString(), event, lang);
      if (interval != -1L) {
         LocalDateTime startDate = this.parseDate(inicio.getAsString(), event, lang, "botextendedmessage.invalidstart");
         if (startDate != null) {
            LocalDateTime endDate = this.parseDate(fin.getAsString(), event, lang, "botextendedmessage.invalidend");
            if (endDate != null) {
               if (!this.validateDates(startDate, endDate, event, lang)) {
                  this.scheduleMessage(event, message.getAsString(), startDate, endDate, interval, lang);
               }
            }
         }
      }
   }

   protected void execute(CommandEvent event) {
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      Pattern pattern = Pattern.compile("\\[(.*?)\\] \\[(.*?)\\] \\[(.*?)\\] \\[(.*?)\\]");
      Matcher matcher = pattern.matcher(event.getArgs());
      if (matcher.find() && matcher.groupCount() >= 4) {
         long interval = this.parseInterval(matcher.group(1), event, lang);
         if (interval != -1L) {
            LocalDateTime startDate = this.parseDate(matcher.group(2), event, lang, "botextendedmessage.invalidstart");
            if (startDate != null) {
               LocalDateTime endDate = this.parseDate(matcher.group(3), event, lang, "botextendedmessage.invalidend");
               if (endDate != null) {
                  if (!this.validateDates(startDate, endDate, event, lang)) {
                     this.scheduleMessage(event, matcher.group(4), startDate, endDate, interval, lang);
                  }
               }
            }
         }
      } else {
         pattern = Pattern.compile("\\[NONE\\] \\[(.*?)\\]");
         matcher = pattern.matcher(event.getArgs());
         if (matcher.find() && matcher.groupCount() == 1) {
            event.reply(lang.getMessage("botextendedmessage.ok"));
            event.getChannel().sendMessage(this.buildMessage(matcher.group(1), LocalDateTime.now(), event.getAuthor())).queue();
         } else {
            event.replyError(lang.getMessage("botextendedmessage.invalidargs"));
         }
      }
   }

   private long parseInterval(String intervalStr, SlashCommandEvent event, LanguageService lang) {
      Pattern pattern = Pattern.compile("(\\d?\\d?\\d?\\d)([DHMS])");
      Matcher matcher = pattern.matcher(intervalStr);
      if (matcher.matches()) {
         return this.getInterval(matcher.group(1), matcher.group(2));
      } else if (intervalStr.equalsIgnoreCase("NONE")) {
         event.reply(lang.getMessage("botextendedmessage.ok")).setEphemeral(true).queue();
         event.getChannel().sendMessage(this.buildMessage(intervalStr, LocalDateTime.now(), event.getUser())).queue();
         return -1L;
      } else {
         event.reply(event.getClient().getError() + " Inserte un intervalo valido, ejemplo: 10S").setEphemeral(true).queue();
         return -1L;
      }
   }

   private LocalDateTime parseDate(String dateStr, SlashCommandEvent event, LanguageService lang, String errorMessage) {
      try {
         return parseWithDefaultYear(dateStr);
      } catch (DateTimeParseException var6) {
         event.reply(event.getClient().getError() + " " + lang.getMessage(errorMessage)).setEphemeral(true).queue();
         return null;
      }
   }

   private boolean validateDates(LocalDateTime startDate, LocalDateTime endDate, SlashCommandEvent event, LanguageService lang) {
      if (startDate.isBefore(LocalDateTime.now())) {
         event.reply(event.getClient().getError() + " La fecha de inicio no puede ser en el pasado").setEphemeral(true).queue();
         return true;
      } else if (startDate.isAfter(endDate)) {
         event.reply(event.getClient().getError() + " La fecha de inicio no puede ser despues de la fecha de fin").setEphemeral(true).queue();
         return true;
      } else {
         return false;
      }
   }

   private void scheduleMessage(SlashCommandEvent event, String message, LocalDateTime startDate, LocalDateTime endDate, long interval, LanguageService lang) {
      ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
      Runnable task = () -> {
         if (LocalDateTime.now().isAfter(endDate)) {
            scheduler.shutdownNow();
         } else {
            event.getChannel().sendMessage(this.buildMessage(message, endDate, event.getUser())).queue();
         }
      };
      scheduler.scheduleAtFixedRate(task, LocalDateTime.now().until(startDate, ChronoUnit.MILLIS), interval, TimeUnit.MILLISECONDS);
      event.reply(
            lang.getSuccessMessage("botextendedmessage.taskcreated", new Object[]{startDate, LocalDateTime.now(), TimeUnit.MILLISECONDS.toSeconds(interval)})
         )
         .setEphemeral(true)
         .queue();
   }

   private long getInterval(String number, String timeunit) {
      switch (timeunit.charAt(0)) {
         case 'D':
            return TimeUnit.DAYS.toMillis(Long.parseLong(number));
         case 'H':
            return TimeUnit.HOURS.toMillis(Long.parseLong(number));
         case 'M':
            return TimeUnit.MINUTES.toMillis(Long.parseLong(number));
         case 'S':
            return TimeUnit.SECONDS.toMillis(Long.parseLong(number));
         default:
            return 0L;
      }
   }

   private String buildMessage(String message, LocalDateTime date, User user) {
      return message.replace("%day%", this.getDay(date))
         .replace("%date%", this.getDate(date))
         .replace("%time%", this.getTime(date))
         .replace("%who%", user.getAsMention())
         .replace("\\n", "\n");
   }

   private String getDay(LocalDateTime date) {
      LocalDateTime now = LocalDateTime.now();
      if (now.getMonthValue() == date.getMonthValue() && now.getDayOfMonth() == date.getDayOfMonth()) {
         return "HOY";
      } else if (now.plusDays(1L).getMonthValue() == date.getMonthValue() && now.plusDays(1L).getDayOfMonth() == date.getDayOfMonth()) {
         return "MAÃ‘ANA";
      } else {
         switch (date.getDayOfWeek()) {
            case MONDAY:
               return "LUNES";
            case TUESDAY:
               return "MARTES";
            case WEDNESDAY:
               return "MIERCOLES";
            case THURSDAY:
               return "JUEVES";
            case FRIDAY:
               return "VIERNES";
            case SATURDAY:
               return "SABADO";
            case SUNDAY:
               return "DOMINGO";
            default:
               return "";
         }
      }
   }

   private String getDate(LocalDateTime date) {
      LocalDateTime now = LocalDateTime.now();
      if (now.getMonthValue() == date.getMonthValue() && now.getDayOfMonth() == date.getDayOfMonth()) {
         return "";
      } else {
         return now.plusDays(1L).getMonthValue() == date.getMonthValue() && now.plusDays(1L).getDayOfMonth() == date.getDayOfMonth()
            ? ""
            : date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
      }
   }

   private static LocalDateTime parseWithDefaultYear(String dateStr) {
      DateTimeFormatter formatter = new DateTimeFormatterBuilder()
         .appendPattern("dd/MM HH:mm:ss")
         .parseDefaulting(ChronoField.YEAR, Calendar.getInstance().get(1))
         .toFormatter(Locale.ENGLISH);
      return LocalDateTime.parse(dateStr, formatter);
   }

   private String getTime(LocalDateTime date) {
      return date.format(DateTimeFormatter.ofPattern("hh.mm a"));
   }

   private long parseInterval(String intervalStr, CommandEvent event, LanguageService lang) {
      Pattern pattern = Pattern.compile("(\\d?\\d?\\d?\\d)([DHMS])");
      Matcher matcher = pattern.matcher(intervalStr);
      if (matcher.matches()) {
         return this.getInterval(matcher.group(1), matcher.group(2));
      } else if (intervalStr.equalsIgnoreCase("NONE")) {
         event.reply(lang.getMessage("botextendedmessage.ok"));
         event.getChannel().sendMessage(this.buildMessage(intervalStr, LocalDateTime.now(), event.getAuthor())).queue();
         return -1L;
      } else {
         event.replyError(lang.getMessage("botextendedmessage.invalidinterval"));
         return -1L;
      }
   }

   private LocalDateTime parseDate(String dateStr, CommandEvent event, LanguageService lang, String errorMessage) {
      try {
         return parseWithDefaultYear(dateStr);
      } catch (DateTimeParseException var6) {
         event.replyError(lang.getMessage(errorMessage));
         return null;
      }
   }

   private boolean validateDates(LocalDateTime startDate, LocalDateTime endDate, CommandEvent event, LanguageService lang) {
      if (startDate.isBefore(LocalDateTime.now())) {
         event.replyError(lang.getMessage("botextendedmessage.startpast"));
         return true;
      } else if (startDate.isAfter(endDate)) {
         event.replyError(lang.getMessage("botextendedmessage.startafterend"));
         return true;
      } else {
         return false;
      }
   }

   private void scheduleMessage(CommandEvent event, String message, LocalDateTime startDate, LocalDateTime endDate, long interval, LanguageService lang) {
      ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
      Runnable task = () -> {
         if (LocalDateTime.now().isAfter(endDate)) {
            scheduler.shutdownNow();
         } else {
            event.getChannel().sendMessage(this.buildMessage(message, endDate, event.getAuthor())).queue();
         }
      };
      scheduler.scheduleAtFixedRate(task, LocalDateTime.now().until(startDate, ChronoUnit.MILLIS), interval, TimeUnit.MILLISECONDS);
      event.reply(
         lang.getSuccessMessage("botextendedmessage.taskcreated", new Object[]{startDate, LocalDateTime.now(), TimeUnit.MILLISECONDS.toSeconds(interval)})
      );
   }
}



