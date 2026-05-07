package com.eme22.bolo.commands.music;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.MusicCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class PlaylistsCmd extends MusicCommand {
   @ConfigProperty(name = "config.aliases.playlists", defaultValue = "")
   String[] aliases = new String[0];

   public PlaylistsCmd(Bot bot) {
      super(bot);
      this.name = "playlists";
      this.help = "shows the available playlists";
      this.guildOnly = true;
      this.beListening = false;
   }

   @Override
   public void doCommand(CommandEvent event) {
      if (!this.bot.getPlaylistLoader().folderExists()) {
         this.bot.getPlaylistLoader().createFolder();
      }

      if (!this.bot.getPlaylistLoader().folderExists()) {
         event.reply(event.getClient().getWarning() + " Playlists folder does not exist and could not be created!");
      } else {
         List<String> list = this.bot.getPlaylistLoader().getPlaylistNames();
         if (list == null) {
            event.reply(event.getClient().getError() + " Failed to load available playlists!");
         } else if (list.isEmpty()) {
            event.reply(event.getClient().getWarning() + " There are no playlists in the Playlists folder!");
         } else {
            StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Available playlists:\n");
            list.forEach(str -> builder.append("`").append(str).append("` "));
            builder.append("\nType `").append(event.getClient().getTextualPrefix()).append("play playlist <name>` to play a playlist");
            event.reply(builder.toString());
         }
      }
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
      if (!this.bot.getPlaylistLoader().folderExists()) {
         this.bot.getPlaylistLoader().createFolder();
      }

      if (!this.bot.getPlaylistLoader().folderExists()) {
         event.reply(event.getClient().getWarning() + " Playlists folder does not exist and could not be created!").queue();
      } else {
         List<String> list = this.bot.getPlaylistLoader().getPlaylistNames();
         if (list == null) {
            event.reply(event.getClient().getError() + " Failed to load available playlists!").queue();
         } else if (list.isEmpty()) {
            event.reply(event.getClient().getWarning() + " There are no playlists in the Playlists folder!").queue();
         } else {
            StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Available playlists:\n");
            list.forEach(str -> builder.append("`").append(str).append("` "));
            builder.append("\nType `").append(event.getClient().getTextualPrefix()).append("play playlist <name>` to play a playlist");
            event.reply(builder.toString()).queue();
         }
      }
   }
}


