package com.eme22.bolo.commands.dj;

import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.audio.AudioLoadResultHandler;
import com.eme22.bolo.audio.QueuedTrack;
import com.eme22.bolo.commands.DJCommand;
import com.eme22.bolo.utils.FormatUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import dev.arbjerg.lavalink.client.FunctionalLoadResultHandler;
import dev.arbjerg.lavalink.client.player.LoadFailed;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.client.player.TrackException;
import dev.arbjerg.lavalink.protocol.v4.Exception.Severity;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Log4j2
public class PlaynextCmd extends DJCommand {
   @ConfigProperty(name = "config.loading")
   private String loadingEmoji;
   @ConfigProperty(name = "config.aliases.repeat", defaultValue = "")
   String[] aliases = new String[0];
   @ConfigProperty(name = "config.maxseconds")
   private long maxSeconds;

   public PlaynextCmd(Bot bot, @Named("djCategory") Category category) {
      super(bot, category);
      this.name = "playnext";
      this.arguments = "<title|URL>";
      this.help = "plays a single song next";
      this.beListening = true;
      this.bePlaying = false;
   }

   @Override
   public void doCommand(CommandEvent event) {
      if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
         event.replyWarning("Please include a song title or URL!");
      } else {
         String args = event.getArgs().startsWith("<") && event.getArgs().endsWith(">")
            ? event.getArgs().substring(1, event.getArgs().length() - 1)
            : (event.getArgs().isEmpty() ? ((Attachment)event.getMessage().getAttachments().get(0)).getUrl() : event.getArgs());
         event.reply(
            this.loadingEmoji + " Loading... `[" + args + "]`",
            m -> this.bot.getPlayerManager().loadItem(event.getGuild(), args, new PlaynextCmd.ResultHandler(m, event, false))
         );
      }
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
   }

   private class ResultHandler implements AudioLoadResultHandler {
      private final Message m;
      private final CommandEvent event;
      private final boolean ytsearch;
      private final FunctionalLoadResultHandler resultHandler = new FunctionalLoadResultHandler(
         trackLoaded -> this.trackLoaded(trackLoaded.getTrack()), this::playlistLoaded, searchResult -> {}, this::noMatches, this::loadFailed
      );

      @Override
      public FunctionalLoadResultHandler getRealResultHandler() {
         return this.resultHandler;
      }

      private ResultHandler(Message m, CommandEvent event, boolean ytsearch) {
         this.m = m;
         this.event = event;
         this.ytsearch = ytsearch;
      }

      private void loadSingle(Track track) {
         if (PlaynextCmd.this.bot.getPlayerManager().isTooLong(track)) {
            this.m
               .editMessage(
                  FormatUtil.filter(
                     this.event.getClient().getWarning()
                        + " This track (**"
                        + track.getInfo().getTitle()
                        + "**) is longer than the allowed maximum: `"
                        + FormatUtil.formatTime(track.getInfo().getLength())
                        + "` > `"
                        + FormatUtil.formatTime(PlaynextCmd.this.maxSeconds * 1000L)
                        + "`"
                  )
               )
               .queue();
         } else {
            AudioHandler handler = PlaynextCmd.this.bot.getPlayerManager().getAudioHandler(this.event.getGuild());
            handler.getQueueManager().addTrackToFront(new QueuedTrack(track, this.event.getAuthor(), this.event.getGuild()));
            int pos = 1;
            String addMsg = FormatUtil.filter(
               this.event.getClient().getSuccess()
                  + " Added **"
                  + track.getInfo().getTitle()
                  + "** (`"
                  + FormatUtil.formatTime(track.getInfo().getLength())
                  + "`) "
                  + (pos == 0 ? "to begin playing" : " to the queue at position " + pos)
            );
            this.m.editMessage(addMsg).queue();
         }
      }

      @Override
      public void trackLoaded(Track track) {
         this.loadSingle(track);
      }

      @Override
      public void playlistLoaded(PlaylistLoaded playlist) {
         Track single;
         if (playlist.getTracks().size() == 1) {
            single = playlist.getInfo().getSelectedTrack() < 0
               ? (Track)playlist.getTracks().get(0)
               : (Track)playlist.getTracks().get(playlist.getInfo().getSelectedTrack());
         } else if (playlist.getInfo().getSelectedTrack() >= 0) {
            single = (Track)playlist.getTracks().get(playlist.getInfo().getSelectedTrack());
         } else {
            single = (Track)playlist.getTracks().get(0);
         }

         this.loadSingle(single);
      }

      @Override
      public void noMatches() {
         if (this.ytsearch) {
            this.m.editMessage(FormatUtil.filter(this.event.getClient().getWarning() + " No results found for `" + this.event.getArgs() + "`.")).queue();
         } else {
            PlaynextCmd.this.bot
               .getPlayerManager()
               .loadItem(this.event.getGuild(), "ytsearch:" + this.event.getArgs(), PlaynextCmd.this.new ResultHandler(this.m, this.event, true));
         }
      }

      @Override
      public void loadFailed(LoadFailed loadFailedData) {
         TrackException throwable = loadFailedData.getException();
         if (throwable.getSeverity() == Severity.COMMON) {
            this.m.editMessage(this.event.getClient().getError() + " Error loading: " + throwable.getMessage()).queue();
         } else {
            this.m.editMessage(this.event.getClient().getError() + " Error loading track.").queue();
         }
      }

      @Override
        public void loadFailed(String error) {
             this.m.editMessage(this.event.getClient().getError() + " Error loading track: " + error).queue();
        }
   }
}



