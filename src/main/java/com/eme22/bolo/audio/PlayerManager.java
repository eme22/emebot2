package com.eme22.bolo.audio;

import com.eme22.bolo.language.LanguageService;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.Link;
import lombok.Generated;

import com.eme22.bolo.Bot;
import com.eme22.bolo.stats.StatsService;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.client.event.*;
import java.util.HashMap;
import java.util.Optional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PlayerManager {
   private static final Logger log = LoggerFactory.getLogger(PlayerManager.class);
   
   private final LavalinkClient client;
   private final StatsService statsService;
   private final boolean stayInChannel;
   private final boolean npImages;
   private final String successEmoji;
   private final NowplayingHandler nowplayingHandler;
   @Inject
   private Bot bot;
   private final HashMap<Long, AudioHandler> handlers = new HashMap<>();

   @Inject
   public PlayerManager(
      @ConfigProperty(name = "config.stayinchannel") boolean stayInChannel,
      @ConfigProperty(name = "config.success") String successEmoji,
      @ConfigProperty(name = "config.nowplayingimages") boolean npImages,
      LavalinkClient client,
      NowplayingHandler nowplayingHandler,
      StatsService statsService
   ) {
      this.client = client;
      this.statsService = statsService;
      this.stayInChannel = stayInChannel;
      this.successEmoji = successEmoji;
      this.nowplayingHandler = nowplayingHandler;
      this.npImages = npImages;
   }

   public AudioHandler getAudioHandler(long guildId) {
      return this.handlers.computeIfAbsent(guildId, id -> new AudioHandler(this, id));
   }

   public AudioHandler getAudioHandler(Guild guild) {
      return this.handlers.computeIfAbsent(guild.getIdLong(), id -> new AudioHandler(this, id));
   }

   public void loadItemOrdered(Object orderingKey, long guildId, String identifier, final AudioLoadResultHandler resultHandler) {
      this.loadItem(guildId, identifier, resultHandler);
   }

   private void loadItemInternal(long guildId, String identifier, final AudioLoadResultHandler resultHandler) {
      try {
         if (!this.hasAvailableNodes()) {
            log.error("Cannot load item for guild {} - no available Lavalink nodes", guildId);
            LanguageService lang = this.bot.getSettingsManager().getLanguageService(guildId);
            resultHandler.loadFailed(lang.getMessage("music.lavalink.unavailable"));
            return;
         }

         AudioHandler audioHandler = this.getAudioHandler(guildId);
         
         Optional<Link> safeLink = audioHandler.getSafeLink();
         if (safeLink.isPresent()) {
            safeLink.get().loadItem(identifier).subscribe(resultHandler.getRealResultHandler());
         } else {
            audioHandler.getLink().loadItem(identifier).subscribe(resultHandler.getRealResultHandler());
         }
      } catch (IllegalStateException e) {
         log.error("Cannot load item for guild {} due to unavailable nodes: {}", guildId, e.getMessage());
         LanguageService lang = this.bot.getSettingsManager().getLanguageService(guildId);
         resultHandler.loadFailed(lang.getMessage("music.lavalink.unavailable"));
      }
   }

   public void loadItem(long guildId, String identifier, final AudioLoadResultHandler resultHandler) {
      this.loadItemInternal(guildId, identifier, resultHandler);
   }

   public void loadItem(Guild guild, String identifier, final AudioLoadResultHandler resultHandler) {
      this.loadItemInternal(guild.getIdLong(), identifier, resultHandler);
   }

   public boolean isTooLong(Track track) {
      return track.getInfo().getLength() > 3600000L;
   }

   public String getMaxTime() {
      return "1";
   }

   public void setUpHandler(Guild guild, VoiceChannel voiceChannel) {
      this.getAudioHandler(guild);
   }

   public void init(Bot bot) {
      this.bot = bot;
      this.nowplayingHandler.init(bot);
      this.subscribeToEvents();
   }

   private void subscribeToEvents() {
      this.client.on(TrackStartEvent.class).subscribe(event -> {
         log.info("Track started in guild {}: {}", event.getGuildId(), event.getTrack().getInfo().getTitle());
         AudioHandler handler = handlers.get(event.getGuildId());
         if (handler != null) handler.onTrackStart(event);
      });
      this.client.on(TrackEndEvent.class).subscribe(event -> {
         log.info("Track ended in guild {}: {} (Reason: {})", event.getGuildId(), event.getTrack().getInfo().getTitle(), event.getEndReason());
         AudioHandler handler = handlers.get(event.getGuildId());
         if (handler != null) handler.onTrackEnd(event);
      });
      this.client.on(TrackExceptionEvent.class).subscribe(event -> {
         log.error("Track exception in guild {}: {} - {}", event.getGuildId(), event.getTrack().getInfo().getTitle(), event.getException().getMessage());
         AudioHandler handler = handlers.get(event.getGuildId());
         if (handler != null) handler.onTrackError(event);
      });
      this.client.on(TrackStuckEvent.class).subscribe(event -> {
         log.error("Track stuck in guild {}: {} (Threshold: {}ms)", event.getGuildId(), event.getTrack().getInfo().getTitle(), event.getThresholdMs());
         // You might want to skip or handle stuck tracks here
      });
      this.client.on(WebSocketClosedEvent.class).subscribe(event -> {
         log.warn("Lavalink WebSocket closed for guild {}: {} - {}", event.getGuildId(), event.getCode(), event.getReason());
         AudioHandler handler = handlers.get(event.getGuildId());
         if (handler != null) handler.onWebSocketClosed(event);
      });
      this.client.on(PlayerUpdateEvent.class).subscribe(event -> {
         AudioHandler handler = handlers.get(event.getGuildId());
         if (handler != null) handler.onPlayerUpdate(event);
      });
      this.client.on(ReadyEvent.class).subscribe(event -> {
         log.info("Lavalink node '{}' ready! Session ID: {}", event.getNode().getName(), event.getSessionId());
      });
   }

   /**
    * Checks if there are any available Lavalink nodes.
    * @return true if at least one node is available, false otherwise
    */
   public boolean hasAvailableNodes() {
      return this.client.getNodes().stream().anyMatch(LavalinkNode::getAvailable);
   }

   /**
    * Gets the count of available nodes.
    * @return the number of available nodes
    */
   public long getAvailableNodeCount() {
      return this.client.getNodes().stream().mapToLong(node -> node.getAvailable() ? 1 : 0).sum();
   }

   @Generated
   public LavalinkClient getClient() {
      return this.client;
   }

   @Generated
   public StatsService getStatsService() {
      return this.statsService;
   }

   @Generated
   public boolean isStayInChannel() {
      return this.stayInChannel;
   }

   @Generated
   public boolean isNpImages() {
      return this.npImages;
   }

   @Generated
   public String getSuccessEmoji() {
      return this.successEmoji;
   }

   @Generated
   public NowplayingHandler getNowplayingHandler() {
      return this.nowplayingHandler;
   }

   @Generated
   public Bot getBot() {
      return this.bot;
   }

   @Generated
   public HashMap<Long, AudioHandler> getHandlers() {
      return this.handlers;
   }

   public void destroyPlayer(long guildId) {
      log.info("Destroying player for guild {}", guildId);
      AudioHandler handler = this.handlers.remove(guildId);
      if (handler != null) {
          try {
              handler.getLink().destroy().subscribe();
          } catch (Exception e) {
              log.error("Error destroying link for guild {}: {}", guildId, e.getMessage());
          }
      }
      
      try {
          this.bot.closeAudioConnection(guildId);
      } catch (Exception e) {
          log.error("Error closing audio connection for guild {}: {}", guildId, e.getMessage());
      }
      
      this.nowplayingHandler.clearLastNPMessage(guildId);
   }

   @Generated
   public void setBot(final Bot bot) {
      this.bot = bot;
   }
}
