package com.eme22.bolo.utils;

import com.eme22.bolo.language.LanguageService;
import java.util.List;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class FormatUtil {
   public static String formatTime(long duration) {
      if (duration == Long.MAX_VALUE) {
         return "LIVE";
      } else {
         long seconds = Math.round(duration / 1000.0);
         long hours = seconds / 3600L;
         seconds %= 3600L;
         long minutes = seconds / 60L;
         seconds %= 60L;
         return (hours > 0L ? hours + ":" : "") + (minutes < 10L ? "0" + minutes : minutes) + ":" + (seconds < 10L ? "0" + seconds : seconds);
      }
   }

   public static String progressBar(double percent) {
      StringBuilder str = new StringBuilder();

      for (int i = 0; i < 12; i++) {
         if (i == (int)(percent * 12.0)) {
            str.append("\ud83d\udd18");
         } else {
            str.append("▬");
         }
      }

      return str.toString();
   }

   public static String volumeIcon(int volume) {
      if (volume == 0) {
         return "\ud83d\udd07";
      } else if (volume < 30) {
         return "\ud83d\udd08";
      } else {
         return volume < 70 ? "\ud83d\udd09" : "\ud83d\udd0a";
      }
   }

   public static String listOfTChannels(List<TextChannel> list, String query) {
      String out = " Multiple text channels found matching \"" + query + "\":";

      for (int i = 0; i < 6 && i < list.size(); i++) {
         out = out + "\n - " + list.get(i).getName() + " (<#" + list.get(i).getId() + ">)";
      }

      if (list.size() > 6) {
         out = out + "\n**And " + (list.size() - 6) + " more...**";
      }

      return out;
   }

   public static String listOfVChannels(List<VoiceChannel> list, String query) {
      String out = " Multiple voice channels found matching \"" + query + "\":";

      for (int i = 0; i < 6 && i < list.size(); i++) {
         out = out + "\n - " + list.get(i).getAsMention() + " (ID:" + list.get(i).getId() + ")";
      }

      if (list.size() > 6) {
         out = out + "\n**And " + (list.size() - 6) + " more...**";
      }

      return out;
   }

   public static String listOfRoles(List<Role> list, String query) {
      String out = " Multiple text channels found matching \"" + query + "\":";

      for (int i = 0; i < 6 && i < list.size(); i++) {
         out = out + "\n - " + list.get(i).getName() + " (ID:" + list.get(i).getId() + ")";
      }

      if (list.size() > 6) {
         out = out + "\n**And " + (list.size() - 6) + " more...**";
      }

      return out;
   }

   public static String filter(String input) {
      return input.replace("\u202e", "").replace("@everyone", "@еveryone").replace("@here", "@hеre").trim();
   }

   public static String formatLocale(LanguageService lang, String key, Object... args) {
      Object[] arguments = new Object[args.length];

      for (int i = 0; i < args.length; i++) {
         if (args[i] instanceof String) {
            arguments[i] = filter((String)args[i]);
         } else {
            arguments[i] = args[i];
         }
      }

      return lang.getMessage(key, arguments);
   }

   public static String formatLocaleWithoutFilter(LanguageService lang, String key, Object... args) {
      return lang.getMessage(key, args);
   }
}
