package com.eme22.bolo;

import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.audio.AloneInVoiceHandler;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.audio.PlayerManager;
import com.eme22.bolo.birthday.BirthdayManager;
import com.eme22.bolo.image.ArtworkImageService;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.playlist.PlaylistLoader;
import com.eme22.bolo.settings.SettingsManager;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jakarta.annotation.PostConstruct;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lombok.Generated;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.runtime.annotations.RegisterForReflection;

@ApplicationScoped
@RegisterForReflection(methods = true)
@Slf4j
public class Bot {
   @Generated
   
   private final EventWaiter waiter;
   private final ScheduledExecutorService threadpool;
   private final SettingsManager settingsManager;
   private final PlayerManager playerManager;
   private final PlaylistLoader playlistLoader;
   private final AloneInVoiceHandler aloneInVoiceHandler;
   private final BirthdayManager birthdayManager;
   private final ArtworkImageService artworkImageService;
   private boolean shuttingDown = false;
   private boolean devMode = false;
   private JDA JDA;
   private Activity activity;
   @ConfigProperty(name = "config.game")
   String game;

   @Inject
   public Bot(
      EventWaiter waiter,
      PlayerManager playerManager,
      AloneInVoiceHandler aloneInvoiceHandler,
      SettingsManager settings,
      ArtworkImageService artworkImageService,
      BirthdayManager birthdayManager
   ) {
      this.waiter = waiter;
      this.settingsManager = settings;
      this.artworkImageService = artworkImageService;
      this.playlistLoader = new PlaylistLoader();
      this.threadpool = Executors.newSingleThreadScheduledExecutor();
      this.playerManager = playerManager;
      this.playerManager.init(this);
      this.aloneInVoiceHandler = aloneInvoiceHandler;
      this.aloneInVoiceHandler.init(this);
      this.birthdayManager = birthdayManager;
   }

   @PostConstruct
   private void init() {
      this.activity = OtherUtil.getActivity(this.game);
   }

   public void closeAudioConnection(long guildId) {
      Guild guild = this.JDA.getGuildById(guildId);
      if (guild != null) {
         this.threadpool.submit(() -> guild.getJDA().getDirectAudioController().disconnect(guild));
      }
   }

   public void resetGame() {
      Activity game = this.activity != null && !this.activity.getName().equalsIgnoreCase("none") ? this.activity : null;
      if (!Objects.equals(this.JDA.getPresence().getActivity(), game)) {
         this.JDA.getPresence().setActivity(game);
      }
   }

   public void shutdown() {
      if (!this.shuttingDown) {
         this.shuttingDown = true;
         this.threadpool.shutdownNow();
         if (this.JDA.getStatus() != Status.SHUTTING_DOWN) {
            this.JDA.getGuilds().forEach(g -> {
               AudioHandler ah = (AudioHandler)g.getAudioManager().getSendingHandler();
               if (ah != null) {
                  ah.stopAndClearSync();
                  LanguageService ls = this.settingsManager.getLanguageService(g);
                  this.playerManager.getNowplayingHandler().updateTopic(g.getIdLong(), ah, true, ls);
               }
            });
            this.JDA.shutdown();
         }


         System.exit(0);
      }
   }

   @Scheduled(
      cron = "0 0 9 * * ?"
   )
   public void remindBirthdays() {
      this.birthdayManager.remindBirthdays(this);
   }

   @Generated
   public EventWaiter getWaiter() {
      return this.waiter;
   }

   @Generated
   public ScheduledExecutorService getThreadpool() {
      return this.threadpool;
   }

   @Generated
   public SettingsManager getSettingsManager() {
      return this.settingsManager;
   }

   @Generated
   public PlayerManager getPlayerManager() {
      return this.playerManager;
   }

   @Generated
   public PlaylistLoader getPlaylistLoader() {
      return this.playlistLoader;
   }

   @Generated
   public AloneInVoiceHandler getAloneInVoiceHandler() {
      return this.aloneInVoiceHandler;
   }

   @Generated
   public BirthdayManager getBirthdayManager() {
      return this.birthdayManager;
   }

   @Generated
   public ArtworkImageService getArtworkImageService() {
      return this.artworkImageService;
   }

   @Generated
   public boolean isShuttingDown() {
      return this.shuttingDown;
   }

   @Generated
   public boolean isDevMode() {
      return this.devMode;
   }


   @Generated
   public JDA getJDA() {
      return this.JDA;
   }

   @Generated
   public Activity getActivity() {
      return this.activity;
   }

   @Generated
   public String getGame() {
      return this.game;
   }

   @Generated
   public void setShuttingDown(final boolean shuttingDown) {
      this.shuttingDown = shuttingDown;
   }

   @Generated
   public void setDevMode(final boolean devMode) {
      this.devMode = devMode;
   }


   @Generated
   public void setJDA(final JDA JDA) {
      this.JDA = JDA;
   }

   @Generated
   public void setActivity(final Activity activity) {
      this.activity = activity;
   }

   @Generated
   public void setGame(final String game) {
      this.game = game;
   }
}
