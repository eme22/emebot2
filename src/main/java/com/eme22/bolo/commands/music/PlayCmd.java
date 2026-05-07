package com.eme22.bolo.commands.music;

import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.audio.AudioLoadResultHandler;
import com.eme22.bolo.audio.QueueManager;
import com.eme22.bolo.audio.QueuedTrack;
import com.eme22.bolo.commands.MusicCommand;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.playlist.PlaylistLoader;
import com.eme22.bolo.utils.FormatUtil;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.menu.ButtonMenu.Builder;
import dev.arbjerg.lavalink.client.FunctionalLoadResultHandler;
import dev.arbjerg.lavalink.client.player.LoadFailed;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.client.player.TrackException;
import dev.arbjerg.lavalink.protocol.v4.Exception.Severity;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Slf4j
public class PlayCmd extends MusicCommand {
   @Generated
   
   private final String loadingEmoji;
   private final long maxSeconds;
   private static final String LOAD = "\ud83d\udce5";
   private static final String CANCEL = "\ud83d\udeab";
   ScheduledExecutorService playListExecutor = Executors.newSingleThreadScheduledExecutor();

   public PlayCmd(
      Bot bot,
      @ConfigProperty(name = "config.aliases.play", defaultValue = "") String[] aliases,
      @ConfigProperty(name = "config.loading") String loadingEmoji,
      @ConfigProperty(name = "config.maxseconds") Long maxSeconds
   ) {
      super(bot);
      this.name = "play";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_US, "play", DiscordLocale.SPANISH, "escuchar", DiscordLocale.SPANISH_LATAM, "escuchar", DiscordLocale.ENGLISH_UK, "play"
      );
      this.arguments = "<title|URL|subcommand>";
      this.help = "plays the provided song";
      this.descriptionLocalization = Map.of(
         DiscordLocale.ENGLISH_US,
         "Plays the provided song",
         DiscordLocale.SPANISH,
         "Reproduce la cancion proporcionada",
         DiscordLocale.SPANISH_LATAM,
         "Reproduce la cancion proporcionada",
         DiscordLocale.ENGLISH_UK,
         "Plays the provided song"
      );
      this.beListening = true;
      this.bePlaying = false;
      this.aliases = aliases;
      this.loadingEmoji = loadingEmoji;
      this.maxSeconds = maxSeconds;
      this.options = List.of(
         new OptionData(OptionType.STRING, "link", "Busca la cancion, playlist o link que desea reproducir.").setRequired(false),
         new OptionData(OptionType.NUMBER, "volumen", "Setea el volumen inicial.").setRequired(false).setMaxValue(100L).setMinValue(0L)
      );
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
      OptionMapping option = event.getOption("link");
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      if (option == null) {
         String builder = event.getClient().getWarning()
            + " "
            + languageService.getMessage("command.play.usage")
            + ":\n\n`"
            + event.getClient().getPrefix()
            + this.name
            + " <"
            + languageService.getMessage("command.play.argument.title")
            + ">` - "
            + languageService.getMessage("command.play.description.title")
            + "\n`"
            + event.getClient().getPrefix()
            + this.name
            + " <URL>` - "
            + languageService.getMessage("command.play.description.url");
         event.reply(builder).setEphemeral(true).queue();
      } else {
         String args = option.getAsString().startsWith("<") && option.getAsString().endsWith(">")
            ? option.getAsString().substring(1, option.getAsString().length() - 1)
            : option.getAsString();
         event.reply(this.loadingEmoji + " " + languageService.getMessage("command.play.loading") + "... `[" + args + "]`")
            .queue(
               s -> s.retrieveOriginal()
                  .queue(
                     m -> this.bot
                        .getPlayerManager()
                        .loadItem(event.getGuild().getIdLong(), args, new PlayCmd.ResultHandler(languageService, m, null, event, false))
                  )
            );
      }
   }

   @Override
   public void doCommand(CommandEvent event) {
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
         StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " " + languageService.getMessage("command.play.usage") + ":\n");
         builder.append("\n`")
            .append(event.getClient().getPrefix())
            .append(this.name)
            .append(" <")
            .append(languageService.getMessage("command.play.argument.title"))
            .append(">` - ")
            .append(languageService.getMessage("command.play.description.title"));
         builder.append("\n`")
            .append(event.getClient().getPrefix())
            .append(this.name)
            .append(" <URL>` - ")
            .append(languageService.getMessage("command.play.description.url"));

         for (Command cmd : this.children) {
            builder.append("\n`")
               .append(event.getClient().getPrefix())
               .append(this.name)
               .append(" ")
               .append(cmd.getName())
               .append(" ")
               .append(cmd.getArguments())
               .append("` - ")
               .append(cmd.getHelp());
         }

         event.reply(builder.toString());
      } else {
         String args = event.getArgs().startsWith("<") && event.getArgs().endsWith(">")
            ? event.getArgs().substring(1, event.getArgs().length() - 1)
            : (event.getArgs().isEmpty() ? ((Attachment)event.getMessage().getAttachments().get(0)).getUrl() : event.getArgs());
         event.reply(
            this.loadingEmoji + " " + languageService.getMessage("command.play.loading") + "... `[" + args + "]`",
            m -> this.bot.getPlayerManager().loadItem(event.getGuild().getIdLong(), args, new PlayCmd.ResultHandler(languageService, m, event, null, false))
         );
      }
   }

   public class PlaylistCmd extends MusicCommand {
      public PlaylistCmd(Bot bot) {
         super(bot);
         this.name = "playlist";
         this.aliases = new String[]{"pl"};
         this.arguments = "<name>";
         this.help = "plays the provided playlist";
         this.beListening = true;
         this.bePlaying = false;
      }

      @Override
      public void doCommand(CommandEvent event) {
         if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + " Please include a playlist name.");
         } else {
            PlaylistLoader.Playlist playlist = this.bot.getPlaylistLoader().getPlaylist(event.getArgs());
            if (playlist == null) {
               event.replyError("I could not find `" + event.getArgs() + ".txt` in the Playlists folder.");
            } else {
               event.getChannel()
                  .sendMessage(PlayCmd.this.loadingEmoji + " Loading playlist **" + event.getArgs() + "**... (" + playlist.getItems().size() + " items)")
                  .queue(
                     m -> {
                        AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
                        playlist.loadTracks(
                           this.bot.getPlayerManager().getAudioHandler(event.getGuild().getIdLong()).getLink(),
                           at -> handler.getQueueManager().addToTrackQueue(new QueuedTrack(at, event.getAuthor(), event.getGuild())),
                           () -> {
                              StringBuilder builder = new StringBuilder(
                                 playlist.getTracks().isEmpty()
                                    ? event.getClient().getWarning() + " No tracks were loaded!"
                                    : event.getClient().getSuccess() + " Loaded **" + playlist.getTracks().size() + "** tracks!"
                              );
                              if (!playlist.getErrors().isEmpty()) {
                                 builder.append("\nThe following tracks failed to load:");
                              }

                              playlist.getErrors()
                                 .forEach(
                                    err -> builder.append("\n`[")
                                       .append(err.index() + 1)
                                       .append("]` **")
                                       .append(err.item())
                                       .append("**: ")
                                       .append(err.reason())
                                 );
                              String str = builder.toString();
                              if (str.length() > 2000) {
                                 str = str.substring(0, 1994) + " (...)";
                              }

                              m.editMessage(FormatUtil.filter(str)).queue();
                           }
                        );
                     }
                  );
            }
         }
      }

      @Override
      public void doCommand(SlashCommandEvent event) {
      }
   }

   @Override
   protected boolean shouldConnect(CommandEvent event) {
      return !event.getArgs().isEmpty() || !event.getMessage().getAttachments().isEmpty();
   }

   @Override
   protected boolean shouldConnect(SlashCommandEvent event) {
      return event.getOption("link") != null;
   }

   private class ResultHandler implements AudioLoadResultHandler {
      private final Message m;
      private final SlashCommandEvent slashEvent;
      private final CommandEvent event;
      private final boolean ytsearch;
      private final LanguageService languageService;
      private final FunctionalLoadResultHandler resultHandler = new FunctionalLoadResultHandler(
         trackLoaded -> this.trackLoaded(trackLoaded.getTrack()),
         this::playlistLoaded,
         searchResult -> this.trackLoaded((Track)searchResult.getTracks().get(0)),
         this::noMatches,
         this::loadFailed
      );

      @Override
      public FunctionalLoadResultHandler getRealResultHandler() {
         return this.resultHandler;
      }

      private ResultHandler(LanguageService languageService, Message m, CommandEvent event, SlashCommandEvent slashEvent, boolean ytsearch) {
         this.languageService = languageService;
         this.m = m;
         this.event = event;
         this.slashEvent = slashEvent;
         this.ytsearch = ytsearch;
      }

      private void loadSingle(Track track, PlaylistLoaded playlist) {
         if (PlayCmd.this.bot.getPlayerManager().isTooLong(track)) {
            this.m
               .editMessage(
                  FormatUtil.formatLocale(
                     this.languageService,
                     "command.play.track_too_long",
                     (this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getWarning(),
                     track.getInfo().getTitle(),
                     FormatUtil.formatTime(track.getInfo().getLength()),
                     FormatUtil.formatTime(PlayCmd.this.maxSeconds * 1000L)
                  )
               )
               .queue();
         } else {
            QueueManager handler = PlayCmd.this.bot
               .getPlayerManager()
               .getAudioHandler((this.slashEvent == null ? this.event.getGuild() : this.slashEvent.getGuild()).getIdLong())
               .getQueueManager();
            int pos = handler.addToTrackQueue(
                  new QueuedTrack(
                     track,
                     this.slashEvent == null ? this.event.getAuthor() : this.slashEvent.getUser(),
                     this.slashEvent == null ? this.event.getGuild() : this.slashEvent.getGuild()
                  )
               )
               + 1;
            String addMsg = FormatUtil.formatLocale(
               this.languageService,
               "command.play.added",
               (this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getSuccess(),
               track.getInfo().getTitle(),
               FormatUtil.formatTime(track.getInfo().getLength()),
               pos == 0
                  ? this.languageService.getMessage("command.play.added_to_begin")
                  : this.languageService.getMessage("command.play.added_to_queue", new Object[]{pos})
            );
            if (playlist != null
               && (this.slashEvent == null ? this.event.getSelfMember() : this.slashEvent.getGuild().getSelfMember())
                  .hasPermission(
                     this.slashEvent == null ? this.event.getTextChannel() : this.slashEvent.getTextChannel(),
                     new Permission[]{Permission.MESSAGE_ADD_REACTION}
                  )) {
               ((Builder)((Builder)new Builder()
                        .setText(
                           addMsg
                              + "\n"
                              + (this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getWarning()
                              + " This track has a playlist of **"
                              + playlist.getTracks().size()
                              + "** tracks attached. Select \ud83d\udce5 to load playlist."
                        )
                        .setChoices(new String[]{"\ud83d\udce5", "\ud83d\udeab"})
                        .setEventWaiter(PlayCmd.this.bot.getWaiter()))
                     .setTimeout(30L, TimeUnit.SECONDS))
                  .setAction(
                     re -> {
                        if (re.getName().equals("\ud83d\udce5")) {
                           this.m
                              .editMessage(
                                 addMsg
                                    + "\n"
                                    + (this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getSuccess()
                                    + " Loaded **"
                                    + this.loadPlaylist(playlist, track)
                                    + "** additional tracks!"
                              )
                              .queue();
                        } else {
                           this.m.editMessage(addMsg).queue();
                        }
                     }
                  )
                  .setFinalAction(m -> {
                     try {
                        m.clearReactions().queue();
                     } catch (PermissionException var2) {
                     }
                  })
                  .build()
                  .display(this.m);
            } else {
               this.m.editMessage(addMsg).queue();
            }
         }
      }

      private int loadPlaylist(PlaylistLoaded playlist, Track exclude) {
         User author = this.slashEvent == null ? this.event.getAuthor() : this.slashEvent.getUser();
         AudioHandler handler = PlayCmd.this.bot
            .getPlayerManager()
            .getAudioHandler((this.slashEvent == null ? this.event.getGuild() : this.slashEvent.getGuild()).getIdLong());
         Track first = (Track)playlist.getTracks().get(0);
         handler.getQueueManager()
            .addToTrackQueue(new QueuedTrack(first, author, (this.slashEvent == null ? this.event.getGuild() : this.slashEvent.getGuild()).getIdLong()));
         handler.getQueueManager()
            .addAllToTrackQueue(
               playlist.getTracks()
                  .stream()
                  .skip(1L)
                  .map(at -> new QueuedTrack(at, author, (this.slashEvent == null ? this.event.getGuild() : this.slashEvent.getGuild()).getIdLong()))
                  .toArray(QueuedTrack[]::new)
            );
         return playlist.getTracks().size() - 1;
      }

      @Override
      public void trackLoaded(Track track) {
         this.loadSingle(track, null);
      }

      @Override
      public void playlistLoaded(PlaylistLoaded playlist) {
         if (playlist.getTracks().size() == 1) {
            Track single = playlist.getInfo().getSelectedTrack() < 0
               ? (Track)playlist.getTracks().get(0)
               : (Track)playlist.getTracks().get(playlist.getInfo().getSelectedTrack());
            this.loadSingle(single, null);
         } else if (playlist.getInfo().getSelectedTrack() >= 0) {
            Track single = (Track)playlist.getTracks().get(playlist.getInfo().getSelectedTrack());
            this.loadSingle(single, playlist);
         } else {
            int count = this.loadPlaylist(playlist, null) - 1;
            if (count == 0) {
               this.m
                  .editMessage(
                     FormatUtil.formatLocale(
                        this.languageService,
                        "command.play.playlist.all_longer",
                        (this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getWarning(),
                        PlayCmd.this.maxSeconds
                     )
                  )
                  .queue();
            } else {
               this.m
                  .editMessage(
                     FormatUtil.formatLocale(
                        this.languageService,
                        "command.play.playlist.loaded",
                        (this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getSuccess(),
                        playlist.getTracks().size(),
                        playlist.getInfo().getName(),
                        count < playlist.getTracks().size()
                           ? "\n"
                              + FormatUtil.formatLocale(
                                 this.languageService,
                                 "command.play.playlist.tracks_omitted",
                                 (this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getWarning(),
                                 PlayCmd.this.bot.getPlayerManager().getMaxTime()
                              )
                           : ""
                     )
                  )
                  .queue();
            }
         }
      }

      @Override
      public void noMatches() {
         if (this.slashEvent == null) {
            if (this.ytsearch) {
               this.m
                  .editMessage(
                     FormatUtil.formatLocale(this.languageService, "command.play.no_results", this.event.getClient().getWarning(), this.event.getArgs())
                  )
                  .queue();
            } else {
               PlayCmd.this.bot
                  .getPlayerManager()
                  .loadItemOrdered(
                     this.event.getClient(),
                     this.event.getGuild().getIdLong(),
                     "ytsearch:" + this.event.getArgs(),
                     PlayCmd.this.new ResultHandler(this.languageService, this.m, this.event, null, true)
                  );
            }
         } else {
            OptionMapping arg = this.slashEvent.getOption("link");
            if (arg == null) {
               if (this.ytsearch) {
                  this.m.editMessage(FormatUtil.filter(this.event.getClient().getWarning() + " No hay resultados.")).queue();
               }
            } else if (this.ytsearch) {
               PlaylistLoader.Playlist playlist = PlayCmd.this.bot.getPlaylistLoader().getPlaylist(this.slashEvent.getOption("link").getAsString());
               if (playlist == null) {
                  this.m.editMessage(FormatUtil.filter(this.event.getClient().getWarning() + " No hay resultados para `" + arg.getAsString() + "`.")).queue();
                  return;
               }

               this.slashEvent
                  .getChannel()
                  .sendMessage(PlayCmd.this.loadingEmoji + " Loading playlist **" + arg.getAsString() + "**... (" + playlist.getItems().size() + " items)")
                  .queue(
                     m -> {
                        AudioHandler handler = PlayCmd.this.bot
                           .getPlayerManager()
                           .getAudioHandler((this.slashEvent == null ? this.event.getGuild() : this.slashEvent.getGuild()).getIdLong());
                        playlist.loadTracks(
                           PlayCmd.this.bot.getPlayerManager().getAudioHandler(this.slashEvent.getGuild().getIdLong()).getLink(),
                           at -> handler.getQueueManager()
                              .addToTrackQueue(new QueuedTrack(at, this.slashEvent.getUser(), this.slashEvent.getGuild().getIdLong())),
                           () -> {
                              StringBuilder builder = new StringBuilder(
                                 playlist.getTracks().isEmpty()
                                    ? this.slashEvent.getClient().getWarning() + " No tracks were loaded!"
                                    : this.slashEvent.getClient().getSuccess() + " Loaded **" + playlist.getTracks().size() + "** tracks!"
                              );
                              if (!playlist.getErrors().isEmpty()) {
                                 builder.append("\nThe following tracks failed to load:");
                              }

                              playlist.getErrors()
                                 .forEach(
                                    err -> builder.append("\n`[")
                                       .append(err.index() + 1)
                                       .append("]` **")
                                       .append(err.item())
                                       .append("**: ")
                                       .append(err.reason())
                                 );
                              String str = builder.toString();
                              if (str.length() > 2000) {
                                 str = str.substring(0, 1994) + " (...)";
                              }

                              m.editMessage(FormatUtil.filter(str)).queue();
                           }
                        );
                     }
                  );
            } else {
               PlayCmd.this.bot
                  .getPlayerManager()
                  .loadItem(
                     this.slashEvent.getGuild().getIdLong(),
                     "ytsearch:" + arg.getAsString(),
                     PlayCmd.this.new ResultHandler(this.languageService, this.m, this.event, this.slashEvent, true)
                  );
            }
         }
      }

      @Override
      public void loadFailed(LoadFailed loadFailed) {
         TrackException throwable = loadFailed.getException();
         if (throwable.getSeverity() == Severity.COMMON) {
            this.m
               .editMessage(
                  (this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getError() + " Error cargando: " + throwable.getMessage()
               )
               .queue();
         } else {
            this.m.editMessage((this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getError() + " Error cargando pista.").queue();
         }
      }

      @Override
      public void loadFailed(String message) {
         this.m.editMessage((this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getError() + " Error cargando pista: " + message).queue();
      }

   }
}



