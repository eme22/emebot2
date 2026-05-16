package com.eme22.bolo.commands.music;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.audio.AudioLoadResultHandler;
import com.eme22.bolo.audio.QueuedTrack;
import com.eme22.bolo.commands.MusicCommand;
import com.eme22.bolo.utils.FormatUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.menu.OrderedMenu.Builder;
import dev.arbjerg.lavalink.client.FunctionalLoadResultHandler;
import dev.arbjerg.lavalink.client.player.LoadFailed;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.client.player.TrackException;
import dev.arbjerg.lavalink.protocol.v4.Exception.Severity;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class SearchCmd extends MusicCommand {
   @ConfigProperty(name = "config.aliases.search", defaultValue = "")
   String[] aliases = new String[0];
   @ConfigProperty(name = "config.searching")
   private String searchingEmoji;
   protected String searchPrefix = "ytsearch:";
   private final Builder builder;

   public SearchCmd(Bot bot) {
      super(bot);
      this.name = "search";
      this.arguments = "<query>";
      this.help = "searches Youtube for a provided query";
      this.beListening = true;
      this.bePlaying = false;
      this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
      this.options = Collections.singletonList(
         new OptionData(OptionType.STRING, "busqueda", "Busca la cancion, playlist o link que desea reproducir.").setRequired(true)
      );
      this.builder = (Builder)((Builder)new Builder().allowTextInput(true).useNumbers().useCancelButton(true).setEventWaiter(bot.getWaiter()))
         .setTimeout(1L, TimeUnit.MINUTES);
   }

   @Override
   public void doCommand(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.replyError("Por favor incluya una busqueda.");
      } else {
         event.reply(
            this.searchingEmoji + " Searching... `[" + event.getArgs() + "]`",
            m -> this.bot.getPlayerManager().loadItem(event.getGuild(), this.searchPrefix + event.getArgs(), new SearchCmd.ResultHandler(m, event, null))
         );
      }
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
      OptionMapping args = event.getOption("busqueda");
      if (args != null && !args.getAsString().isEmpty()) {
         event.reply(this.searchingEmoji + " Searching... `[" + args.getAsString().isEmpty() + "]`")
            .queue(
               s -> s.retrieveOriginal()
                  .queue(
                     m -> this.bot
                        .getPlayerManager()
                        .loadItem(event.getGuild(), this.searchPrefix + args.getAsString().isEmpty(), new SearchCmd.ResultHandler(m, null, event))
                  )
            );
      } else {
         event.reply(event.getClient().getWarning() + "Please include a query.").queue();
      }
   }

   private class ResultHandler implements AudioLoadResultHandler {
      private final Message m;
      private final CommandEvent event;
      private final SlashCommandEvent slashEvent;
      private final String args;
      private final FunctionalLoadResultHandler resultHandler = new FunctionalLoadResultHandler(
         trackLoaded -> this.trackLoaded(trackLoaded.getTrack()), this::playlistLoaded, searchResult -> {}, this::noMatches, this::loadFailed
      );

      @Override
      public FunctionalLoadResultHandler getRealResultHandler() {
         return this.resultHandler;
      }

      private ResultHandler(Message m, CommandEvent event, SlashCommandEvent slashEvent) {
         this.m = m;
         this.event = event;
         this.slashEvent = slashEvent;
         this.args = slashEvent != null ? (String)slashEvent.getOption("busqueda", OptionMapping::getAsString) : event.getArgs();
      }

      @Override
      public void trackLoaded(Track track) {
         if (SearchCmd.this.bot.getPlayerManager().isTooLong(track)) {
            this.m
               .editMessage(
                  FormatUtil.filter(
                     (this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getWarning()
                        + " This track (**"
                        + track.getInfo().getTitle()
                        + "**) is longer than the allowed maximum: `"
                        + FormatUtil.formatTime(track.getInfo().getLength())
                        + "` > `"
                        + SearchCmd.this.bot.getPlayerManager().getMaxTime()
                        + "`"
                  )
               )
               .queue();
         } else {
            AudioHandler handler = (AudioHandler)(this.slashEvent == null ? this.event.getGuild() : this.slashEvent.getGuild())
               .getAudioManager()
               .getSendingHandler();
            int pos = handler.getQueueManager()
                  .addToTrackQueue(
                     new QueuedTrack(
                        track,
                        this.slashEvent == null ? this.event.getAuthor() : this.slashEvent.getUser(),
                        (this.slashEvent == null ? this.event.getGuild() : this.slashEvent.getGuild()).getIdLong()
                     )
                  )
               + 1;
            this.m
               .editMessage(
                  FormatUtil.filter(
                     (this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getSuccess()
                        + " Added **"
                        + track.getInfo().getTitle()
                        + "** (`"
                        + FormatUtil.formatTime(track.getInfo().getLength())
                        + "`) "
                        + (pos == 0 ? "to begin playing" : " to the queue at position " + pos)
                  )
               )
               .queue();
         }
      }

      @Override
      public void playlistLoaded(PlaylistLoaded playlist) {
         SearchCmd.this.builder
            .setColor((this.slashEvent == null ? this.event.getGuild().getSelfMember() : this.slashEvent.getGuild().getSelfMember()).getColor())
            .setText(
               FormatUtil.filter(
                  (this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getSuccess()
                     + " Search results for `"
                     + (this.slashEvent == null ? this.event.getArgs() : this.args)
                     + "`:"
               )
            )
            .setChoices(new String[0])
            .setSelection(
               (msg, ix) -> {
                  Track trackx = (Track)playlist.getTracks().get(ix - 1);
                  if (SearchCmd.this.bot.getPlayerManager().isTooLong(trackx)) {
                     if (this.slashEvent == null) {
                        this.event
                           .replyWarning(
                              "This track (**"
                                 + trackx.getInfo().getTitle()
                                 + "**) is longer than the allowed maximum: `"
                                 + FormatUtil.formatTime(trackx.getInfo().getLength())
                                 + "` > `"
                                 + SearchCmd.this.bot.getPlayerManager().getMaxTime()
                                 + "`"
                           );
                     } else {
                        this.slashEvent
                           .reply(
                              this.event.getClient().getWarning()
                                 + "This track (**"
                                 + trackx.getInfo().getTitle()
                                 + "**) is longer than the allowed maximum: `"
                                 + FormatUtil.formatTime(trackx.getInfo().getLength())
                                 + "` > `"
                                 + SearchCmd.this.bot.getPlayerManager().getMaxTime()
                                 + "`"
                           )
                           .queue();
                     }
                  } else {
                     AudioHandler handler = (AudioHandler)(this.slashEvent == null ? this.event.getGuild() : this.slashEvent.getGuild())
                        .getAudioManager()
                        .getSendingHandler();
                     int pos = handler.getQueueManager()
                           .addToTrackQueue(
                              new QueuedTrack(
                                 trackx,
                                 this.slashEvent == null ? this.event.getAuthor() : this.slashEvent.getUser(),
                                 (this.slashEvent == null ? this.event.getGuild() : this.slashEvent.getGuild()).getIdLong()
                              )
                           )
                        + 1;
                     this.event
                        .replySuccess(
                           "Added **"
                              + FormatUtil.filter(trackx.getInfo().getTitle())
                              + "** (`"
                              + FormatUtil.formatTime(trackx.getInfo().getLength())
                              + "`) "
                              + (pos == 0 ? "to begin playing" : " to the queue at position " + pos)
                        );
                  }
               }
            )
            .setCancel(msg -> {})
            .setUsers(new User[]{this.slashEvent == null ? this.event.getAuthor() : this.slashEvent.getUser()});

         for (int i = 0; i < 4 && i < playlist.getTracks().size(); i++) {
            Track track = (Track)playlist.getTracks().get(i);
            SearchCmd.this.builder
               .addChoices(
                  new String[]{
                     "`["
                        + FormatUtil.formatTime(track.getInfo().getLength())
                        + "]` [**"
                        + track.getInfo().getTitle()
                        + "**]("
                        + track.getInfo().getUri()
                        + ")"
                  }
               );
         }

         SearchCmd.this.builder.build().display(this.m);
      }

      @Override
      public void noMatches() {
         this.m
            .editMessage(
               FormatUtil.filter(
                  (this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getWarning()
                     + " No results found for `"
                     + (this.slashEvent == null ? this.event.getArgs() : this.args)
                     + "`."
               )
            )
            .queue();
      }

      @Override
      public void loadFailed(LoadFailed loadFailedData) {
         TrackException throwable = loadFailedData.getException();
         if (throwable.getSeverity() == Severity.COMMON) {
            this.m
               .editMessage(
                  (this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getError() + " Error loading: " + throwable.getMessage()
               )
               .queue();
         } else {
            this.m.editMessage((this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getError() + " Error loading track.").queue();
         }
      }

      @Override
        public void loadFailed(String error) {
         this.m.editMessage((this.slashEvent == null ? this.event.getClient() : this.slashEvent.getClient()).getError() + " Error loading track: " + error).queue();
      }
   }
}







