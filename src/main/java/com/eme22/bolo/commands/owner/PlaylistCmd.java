package com.eme22.bolo.commands.owner;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.OwnerCommand;
import com.eme22.bolo.playlist.PlaylistLoader;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class PlaylistCmd extends OwnerCommand {
   private final Bot bot;
   @ConfigProperty(name = "config.aliases.playlist", defaultValue = "")
   String[] aliases = new String[0];

   public PlaylistCmd(Bot bot) {
      this.bot = bot;
      this.guildOnly = false;
      this.name = "playlist";
      this.arguments = "<append|delete|make|setdefault>";
      this.help = "playlist management";
      this.children = new OwnerCommand[]{
         new PlaylistCmd.ListCmd(),
         new PlaylistCmd.AppendlistCmd(),
         new PlaylistCmd.DeletelistCmd(),
         new PlaylistCmd.MakelistCmd(),
         new PlaylistCmd.DefaultlistCmd(bot)
      };
   }

   public void execute(SlashCommandEvent event) {
      StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " Playlist Management Commands:\n");

      for (Command cmd : this.children) {
         builder.append("\n`")
            .append(event.getClient().getPrefix())
            .append(this.name)
            .append(" ")
            .append(cmd.getName())
            .append(" ")
            .append(cmd.getArguments() == null ? "" : cmd.getArguments())
            .append("` - ")
            .append(cmd.getHelp());
      }

      event.reply(builder.toString()).queue();
   }

   public void execute(CommandEvent event) {
      StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " Playlist Management Commands:\n");

      for (Command cmd : this.children) {
         builder.append("\n`")
            .append(event.getClient().getPrefix())
            .append(this.name)
            .append(" ")
            .append(cmd.getName())
            .append(" ")
            .append(cmd.getArguments() == null ? "" : cmd.getArguments())
            .append("` - ")
            .append(cmd.getHelp());
      }

      event.reply(builder.toString());
   }
public class AppendlistCmd extends OwnerCommand {
      public AppendlistCmd() {
         this.name = "append";
         this.aliases = new String[]{"add"};
         this.help = "appends songs to an existing playlist";
         this.arguments = "<name> <URL> | <URL> | ...";
         this.guildOnly = false;
         this.options = Arrays.asList(
            new OptionData(OptionType.STRING, "name", "nombre de la playlist").setRequired(true),
            new OptionData(OptionType.STRING, "url1", "link del video 1").setRequired(true),
            new OptionData(OptionType.STRING, "url2", "link del video 2").setRequired(true),
            new OptionData(OptionType.STRING, "url3", "link del video 3").setRequired(true),
            new OptionData(OptionType.STRING, "url4", "link del video 4").setRequired(true),
            new OptionData(OptionType.STRING, "url5", "link del video 5").setRequired(true)
         );
      }

      public void execute(SlashCommandEvent event) {
         String pname = (String)event.getOption("name", OptionMapping::getAsString);
         String[] parts = event.getOptionsByName("url").stream().map(OptionMapping::getAsString).toArray(String[]::new);
         StringBuilder builder = new StringBuilder();

         for (String url : parts) {
            builder.append("\r\n").append(url);
         }

         try {
            PlaylistCmd.this.bot.getPlaylistLoader().writePlaylist(pname, builder.toString());
            event.reply(event.getClient().getSuccess() + " Successfully added " + parts.length + " items to playlist `" + pname + "`!").queue();
         } catch (IOException var9) {
            event.reply(event.getClient().getError() + " I was unable to append to the playlist: " + var9.getLocalizedMessage()).queue();
         }
      }

      public void execute(CommandEvent event) {
         String[] parts = event.getArgs().split("\\s+", 2);
         if (parts.length < 2) {
            event.reply(event.getClient().getError() + " Please include a playlist name and URLs to add!");
         } else {
            String pname = parts[0];
            PlaylistLoader.Playlist playlist = PlaylistCmd.this.bot.getPlaylistLoader().getPlaylist(pname);
            if (playlist == null) {
               event.reply(event.getClient().getError() + " Playlist `" + pname + "` doesn't exist!");
            } else {
               StringBuilder builder = new StringBuilder();
               playlist.getItems().forEach(item -> builder.append("\r\n").append(item));
               String[] urls = parts[1].split("\\|");

               for (String url : urls) {
                  String u = url.trim();
                  if (u.startsWith("<") && u.endsWith(">")) {
                     u = u.substring(1, u.length() - 1);
                  }

                  builder.append("\r\n").append(u);
               }

               try {
                  PlaylistCmd.this.bot.getPlaylistLoader().writePlaylist(pname, builder.toString());
                  event.reply(event.getClient().getSuccess() + " Successfully added " + urls.length + " items to playlist `" + pname + "`!");
               } catch (IOException var12) {
                  event.reply(event.getClient().getError() + " I was unable to append to the playlist: " + var12.getLocalizedMessage());
               }
            }
         }
      }
   }
public class DefaultlistCmd extends AutoplaylistCmd {
      public DefaultlistCmd(Bot bot) {
         super(bot);
         this.name = "setdefault";
         this.aliases = new String[]{"default"};
         this.arguments = "<playlistname|NONE>";
         this.guildOnly = true;
      }
   }
public class DeletelistCmd extends OwnerCommand {
      public DeletelistCmd() {
         this.name = "delete";
         this.aliases = new String[]{"remove"};
         this.help = "deletes an existing playlist";
         this.arguments = "<name>";
         this.guildOnly = false;
         this.options = Collections.singletonList(new OptionData(OptionType.STRING, "name", "Playlist name").setRequired(true));
      }

      public void execute(SlashCommandEvent event) {
         String pname = (String)event.getOption("name", OptionMapping::getAsString);
         if (PlaylistCmd.this.bot.getPlaylistLoader().getPlaylist(pname) == null) {
            event.reply(event.getClient().getError() + " Playlist `" + pname + "` doesn't exist!").queue();
         } else {
            try {
               PlaylistCmd.this.bot.getPlaylistLoader().deletePlaylist(pname);
               event.reply(event.getClient().getSuccess() + " Successfully deleted playlist `" + pname + "`!").queue();
            } catch (IOException var4) {
               event.reply(event.getClient().getError() + " I was unable to delete the playlist: " + var4.getLocalizedMessage()).queue();
            }
         }
      }

      public void execute(CommandEvent event) {
         String pname = event.getArgs().replaceAll("\\s+", "_");
         if (PlaylistCmd.this.bot.getPlaylistLoader().getPlaylist(pname) == null) {
            event.reply(event.getClient().getError() + " Playlist `" + pname + "` doesn't exist!");
         } else {
            try {
               PlaylistCmd.this.bot.getPlaylistLoader().deletePlaylist(pname);
               event.reply(event.getClient().getSuccess() + " Successfully deleted playlist `" + pname + "`!");
            } catch (IOException var4) {
               event.reply(event.getClient().getError() + " I was unable to delete the playlist: " + var4.getLocalizedMessage());
            }
         }
      }
   }
public class ListCmd extends OwnerCommand {
      public ListCmd() {
         this.name = "all";
         this.aliases = new String[]{"available", "list"};
         this.help = "lists all available playlists";
         this.guildOnly = true;
      }

      public void execute(SlashCommandEvent event) {
         if (!PlaylistCmd.this.bot.getPlaylistLoader().folderExists()) {
            PlaylistCmd.this.bot.getPlaylistLoader().createFolder();
         }

         if (!PlaylistCmd.this.bot.getPlaylistLoader().folderExists()) {
            event.reply(event.getClient().getWarning() + " Playlists folder does not exist and could not be created!").queue();
         } else {
            List<String> list = PlaylistCmd.this.bot.getPlaylistLoader().getPlaylistNames();
            if (list == null) {
               event.reply(event.getClient().getError() + " Failed to load available playlists!").queue();
            } else if (list.isEmpty()) {
               event.reply(event.getClient().getWarning() + " There are no playlists in the Playlists folder!").queue();
            } else {
               StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Available playlists:\n");
               list.forEach(str -> builder.append("`").append(str).append("` "));
               event.reply(builder.toString()).queue();
            }
         }
      }

      public void execute(CommandEvent event) {
         if (!PlaylistCmd.this.bot.getPlaylistLoader().folderExists()) {
            PlaylistCmd.this.bot.getPlaylistLoader().createFolder();
         }

         if (!PlaylistCmd.this.bot.getPlaylistLoader().folderExists()) {
            event.replyWarning(" Playlists folder does not exist and could not be created!");
         } else {
            List<String> list = PlaylistCmd.this.bot.getPlaylistLoader().getPlaylistNames();
            if (list == null) {
               event.replyError(" Failed to load available playlists!");
            } else if (list.isEmpty()) {
               event.replyWarning(" There are no playlists in the Playlists folder!");
            } else {
               StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Available playlists:\n");
               list.forEach(str -> builder.append("`").append(str).append("` "));
               event.reply(builder.toString());
            }
         }
      }
   }
public class MakelistCmd extends OwnerCommand {
      public MakelistCmd() {
         this.name = "make";
         this.aliases = new String[]{"create"};
         this.help = "makes a new playlist";
         this.arguments = "<name>";
         this.guildOnly = false;
         this.options = Collections.singletonList(new OptionData(OptionType.STRING, "name", "Playlist name").setRequired(true));
      }

      public void execute(SlashCommandEvent event) {
         String pname = (String)event.getOption("name", OptionMapping::getAsString);
         if (PlaylistCmd.this.bot.getPlaylistLoader().getPlaylist(pname) == null) {
            try {
               PlaylistCmd.this.bot.getPlaylistLoader().createPlaylist(pname);
               event.reply(event.getClient().getSuccess() + " Successfully created playlist `" + pname + "`!").queue();
            } catch (IOException var4) {
               event.reply(event.getClient().getError() + " I was unable to create the playlist: " + var4.getLocalizedMessage()).queue();
            }
         } else {
            event.reply(event.getClient().getError() + " Playlist `" + pname + "` already exists!").queue();
         }
      }

      public void execute(CommandEvent event) {
         String pname = event.getArgs().replaceAll("\\s+", "_");
         if (PlaylistCmd.this.bot.getPlaylistLoader().getPlaylist(pname) == null) {
            try {
               PlaylistCmd.this.bot.getPlaylistLoader().createPlaylist(pname);
               event.replySuccess(" Successfully created playlist `" + pname + "`!");
            } catch (IOException var4) {
               event.replyError(" I was unable to create the playlist: " + var4.getLocalizedMessage());
            }
         } else {
            event.replyError(" Playlist `" + pname + "` already exists!");
         }
      }
   }
}











