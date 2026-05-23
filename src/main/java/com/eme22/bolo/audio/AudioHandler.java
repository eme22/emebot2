package com.eme22.bolo.audio;

import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.model.MusicArtWork;
import com.eme22.bolo.model.RepeatMode;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.playlist.PlaylistLoader;
import com.eme22.bolo.utils.FormatUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.event.PlayerUpdateEvent;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackExceptionEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import dev.arbjerg.lavalink.client.event.WebSocketClosedEvent;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.client.player.FilterBuilder;
import dev.arbjerg.lavalink.protocol.v4.Timescale;
import dev.arbjerg.lavalink.protocol.v4.Karaoke;
import dev.arbjerg.lavalink.protocol.v4.Distortion;
import dev.arbjerg.lavalink.protocol.v4.Band;
import java.io.IOException;
import java.lang.Exception;
import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason;
@Slf4j
public class AudioHandler {
   
   private final PlayerManager playerManager;
   private final Set<String> votes = new HashSet<>();
   private final long guildId;
   private final QueueManager queueManager;
   private final List<Track> defaultQueue = new LinkedList<>();
   private final ObjectMapper mapper = new ObjectMapper();
   private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
   private volatile int reconnectAttempts = 0;
   private static final int MAX_RECONNECT_ATTEMPTS = 3;
   private volatile LavalinkPlayer cachedPlayer;
   private int previousVolume = 100;
   private boolean isMuted = false;
   private String currentEffect = "effect_none";

   public AudioHandler(PlayerManager playerManager, long guildId) {
      this.playerManager = playerManager;
      this.guildId = guildId;
      this.queueManager = new QueueManager(this);
      
      // Pre-fetch player state asynchronously
      this.getLink().getPlayer().subscribe(player -> this.cachedPlayer = player);
   }

   public Link getLink() {
      Link cachedLink = this.playerManager.getClient().getLinkIfCached(this.guildId);
      if (cachedLink != null) {
         // Check if the cached link's node is still available
         if (cachedLink.getNode() != null && cachedLink.getNode().getAvailable()) {
            return cachedLink;
         } else {
            // The cached link points to an unavailable node, force recreation
            log.warn("Cached link for guild {} points to unavailable node, forcing recreation", this.guildId);
            // Clear the cached link by attempting to get a new one
            // The framework should handle switching to a different available node
         }
      }
      return this.createLink();
   }

   public Link createLink() {
      try {
         // Check if any nodes are available before attempting to create a link
         if (!this.playerManager.hasAvailableNodes()) {
            log.warn("No available nodes for guild {}, cannot create link", this.guildId);
            throw new IllegalStateException("No Lavalink nodes are currently available");
         }

         Link link = this.playerManager.getClient().getOrCreateLink(this.guildId);
         // Reset reconnect attempts on successful connection
         this.reconnectAttempts = 0;
         log.debug("Successfully created/retrieved link for guild {}", this.guildId);
         return link;
      } catch (IllegalStateException e) {
         this.reconnectAttempts++;
         log.warn("Failed to create link for guild {} (attempt {}/{}): {}", 
                  this.guildId, this.reconnectAttempts, MAX_RECONNECT_ATTEMPTS, e.getMessage());
         
         if (this.reconnectAttempts <= MAX_RECONNECT_ATTEMPTS) {
            // Schedule a retry with exponential backoff - but don't block the current thread
            long delay = 5L * this.reconnectAttempts;
            log.info("Scheduling retry for guild {} in {} seconds (attempt {}/{})", 
                     this.guildId, delay, this.reconnectAttempts, MAX_RECONNECT_ATTEMPTS);
            
            executor.schedule(() -> {
               try {
                  if (this.playerManager.hasAvailableNodes()) {
                     this.playerManager.getClient().getOrCreateLink(this.guildId);
                     log.info("Successfully reconnected to Lavalink for guild {} after {} attempts", 
                              this.guildId, this.reconnectAttempts);
                     this.reconnectAttempts = 0; // Reset on success
                  } else {
                     log.warn("Still no available nodes for guild {} during retry attempt {}", 
                              this.guildId, this.reconnectAttempts);
                  }
               } catch (IllegalStateException retryException) {
                  log.error("Failed to reconnect to Lavalink for guild {} after retry attempt {}: {}", 
                           this.guildId, this.reconnectAttempts, retryException.getMessage());
               }
            }, delay, TimeUnit.SECONDS);
         } else {
            log.error("Max reconnection attempts ({}) reached for guild {}. Resetting retry counter.", 
                     MAX_RECONNECT_ATTEMPTS, this.guildId);
            this.reconnectAttempts = 0; // Reset for future attempts
         }
         
         throw e; // Re-throw the exception to maintain current behavior
      }
   }

   public Optional<LavalinkPlayer> getAudioPlayer() {
      if (this.cachedPlayer != null) {
         return Optional.of(this.cachedPlayer);
      }
      
      try {
         // Try to get the player synchronously with a timeout
         this.cachedPlayer = this.getLink().getPlayer().block(Duration.ofSeconds(3));
         return Optional.ofNullable(this.cachedPlayer);
      } catch (Exception e) {
         log.warn("Failed to block for player for guild {}: {}", this.guildId, e.getMessage());
         
         // Fallback to async if blocking fails/times out
         this.getLink().getPlayer().subscribe(player -> this.cachedPlayer = player);
         return Optional.empty();
      }
   }

   public void onPlayerUpdate(PlayerUpdateEvent event) {
      this.getLink().getPlayer().subscribe(player -> this.cachedPlayer = player);
   }

   public void playTrack(Track track) {
      this.getAudioPlayer().ifPresent(player -> player.setPaused(false).setTrack(track).subscribe());
   }

   // Subscriptions moved to PlayerManager

   public void onTrackStart(TrackStartEvent event) {
      log.debug("Track started: {}", event.getTrack().getInfo().getTitle());

      try {
         RequestMetadata metadata = event.getTrack().getUserData(RequestMetadata.class);
         if (metadata != null && metadata.user().guild() != this.guildId) {
            return;
         }

         this.votes.clear();
         Guild guild = this.playerManager.getBot().getJDA().getGuildById(this.guildId);
         LanguageService lang = this.playerManager.getBot().getSettingsManager().getLanguageService(this.guildId);
         this.playerManager.getNowplayingHandler().onTrackUpdate(this.guildId, event.getTrack(), this, lang);
         MessageCreateData m = this.getNowPlaying(this.playerManager.getBot().getJDA(), event.getTrack());
         this.playerManager.getStatsService().increment(guild.getIdLong(), "SONGS_PLAYED");
         if (m == null) {
            TextChannel chn = guild.getTextChannelById(this.playerManager.getBot().getSettingsManager().getSettings(guild).getTextChannelId());
            if (chn == null) {
               chn = guild.getDefaultChannel().asTextChannel();
            }

            chn.sendMessage(this.getNoMusicPlaying(this.playerManager.getBot().getJDA(), lang)).queue();
            this.playerManager.getNowplayingHandler().clearLastNPMessage(guild);
         } else {
            this.playerManager.getNowplayingHandler().clearLastNPMessage(guild);
            TextChannel chn = guild.getTextChannelById(this.playerManager.getBot().getSettingsManager().getSettings(guild).getTextChannelId());
            if (chn == null) {
               chn = this.playerManager.getBot().getJDA().getGuildById(this.guildId).getDefaultChannel().asTextChannel();
            }

            chn.sendMessage(m).queue(msg -> {
               this.playerManager.getNowplayingHandler().setLastNPMessage(msg);
            });
         }
      } catch (Exception var6) {
         log.error("Error: " + var6.getMessage(), var6);
      }
   }

   public void onTrackError(TrackExceptionEvent event) {
      log.error("Track error: {} - {}", event.getTrack().getInfo().getTitle(), event.getException().getMessage());

      try {
         LanguageService lang = this.playerManager.getBot().getSettingsManager().getLanguageService(this.guildId);
         this.playerManager.getNowplayingHandler().onTrackUpdate(this.guildId, null, this, lang);
         
         if (this.queueManager.getQueue().isEmpty()) {
            if (!this.playFromDefault()) {
               this.playerManager.getNowplayingHandler().onTrackUpdate(this.guildId, null, this, lang);
               if (!this.playerManager.isStayInChannel()) {
                   this.playerManager.destroyPlayer(this.guildId);
               }
            }
         } else {
            QueuedTrack qt = this.queueManager.getQueue().pull();
            this.getAudioPlayer().ifPresent(player -> player.setPaused(false).setTrack(qt.getTrack()).subscribe());
         }
      } catch (Exception var3) {
         log.error("Error: {}", var3.getMessage(), var3);
      }
   }

   public boolean isMusicPlaying(JDA jda) {
      return this.isMusicPlaying(jda, null);
   }

   public boolean isMusicPlaying(JDA jda, Track track) {
      return this.guild(jda).getSelfMember().getVoiceState().inAudioChannel()
         && (track != null || (this.getAudioPlayer().isPresent() && this.getAudioPlayer().get().getTrack() != null));
   }

   public RequestMetadata getRequestMetadata() throws IOException {
      return this.getRequestMetadata(null);
   }

   public RequestMetadata getRequestMetadata(Track track) throws IOException {
      if (track == null) {
         Optional<LavalinkPlayer> player = this.getAudioPlayer();
         if (player.isEmpty() || player.get().getTrack() == null) {
            return RequestMetadata.EMPTY;
         }
         track = player.get().getTrack();
      }

      Object userData = track.getUserData();
      if (userData == null) return RequestMetadata.EMPTY;
      
      if (userData instanceof RequestMetadata) {
         return (RequestMetadata) userData;
      }
      
      try {
         return (RequestMetadata) this.mapper.readValue(userData.toString(), RequestMetadata.class);
      } catch (Exception e) {
         log.error("Error reading track metadata: {}", e.getMessage());
         return RequestMetadata.EMPTY;
      }
   }

   public MessageCreateData getNowPlaying(JDA jda) {
      return this.getNowPlaying(jda, null);
   }

   public MessageCreateData getNowPlaying(JDA jda, Track track) {
      if (this.isMusicPlaying(jda, track)) {
         Guild guild = this.guild(jda);
         LavalinkPlayer audioPlayer = this.getAudioPlayer().get();
         LanguageService lang = this.playerManager.getBot().getSettingsManager().getLanguageService(guild);
         if (track == null) track = audioPlayer.getTrack();
         MessageCreateBuilder builder = new MessageCreateBuilder();
         String mb = FormatUtil.formatLocale(
            lang, "music.nowplaying", this.playerManager.getSuccessEmoji(), Optional.ofNullable(guild.getSelfMember().getVoiceState().getChannel()).map(c -> c.getAsMention()).orElse("Unknown channel")
         );
         EmbedBuilder eb = new EmbedBuilder();
         eb.setColor(guild.getSelfMember().getColor());
         RequestMetadata rm = null;

         try {
            rm = this.getRequestMetadata(track);
         } catch (IOException var15) {
            throw new RuntimeException(var15);
         }

         if (rm.owner() != 0L) {
            User u = guild.getJDA().getUserById(rm.user().id());
            if (u == null) {
               eb.setAuthor(rm.user().username(), null, rm.user().avatar());
            } else {
               eb.setAuthor(u.getName(), null, u.getEffectiveAvatarUrl());
            }
         }

         try {
            eb.setTitle(track.getInfo().getTitle(), track.getInfo().getUri());
         } catch (Exception var14) {
            eb.setTitle(track.getInfo().getTitle());
         }

         if (track.getInfo().getAuthor() != null && !track.getInfo().getAuthor().isEmpty()) {
            eb.setFooter(FormatUtil.formatLocaleWithoutFilter(lang, "music.source", track.getInfo().getAuthor()), null);
         }

         RepeatMode repeatMode = this.playerManager.getBot().getSettingsManager().getSettings(this.guildId).getRepeatMode();
         String repeatStr = (repeatMode.getEmoji() != null ? repeatMode.getEmoji() + " " : "") + lang.getMessage("music.repeat." + repeatMode.getKey());
         eb.addField(lang.getMessage("music.repeat.mode"), repeatStr, true);

         if (!"effect_none".equals(currentEffect)) {
             String effectName = lang.getMessage(currentEffect.replace("_", ".") + ".label");
             eb.addField(lang.getMessage("music.effect.active"), effectName, true);
         }

         double progress = (double)audioPlayer.getPosition() / track.getInfo().getLength();
         
         StringBuilder description = new StringBuilder();
         description.append(audioPlayer.getPaused() ? "⏸" : "▶")
                    .append(" ")
                    .append(FormatUtil.progressBar(progress))
                    .append(" `[")
                    .append(FormatUtil.formatTime(audioPlayer.getPosition()))
                    .append("/")
                    .append(FormatUtil.formatTime(track.getInfo().getLength()))
                    .append("]` ")
                    .append(FormatUtil.volumeIcon(audioPlayer.getVolume()));
         
         if (!this.queueManager.getQueue().isEmpty()) {
             QueuedTrack nextTrack = this.queueManager.getQueue().get(0);
             description.append("\n\n**")
                        .append(lang.getMessage("music.next"))
                        .append(":** ")
                        .append(nextTrack.getTrack().getInfo().getTitle());
         }
         
         eb.setDescription(description.toString());

         eb.setFooter(FormatUtil.formatLocaleWithoutFilter(lang, "music.queue.status", 
             this.queueManager.getQueue().size(), 
             FormatUtil.formatTime(this.queueManager.getQueue().getList().stream().mapToLong(t -> t.getTrack().getInfo().getLength()).sum())), null);
         String author = track.getInfo().getAuthor().toLowerCase();
         Optional<MusicArtWork> artWork = this.playerManager.getBot().getArtworkImageService().getArtwork(author);
         if (artWork.isPresent()) {
            eb.setImage(artWork.get().getUrl());
         } else if ("spotify".equals(track.getInfo().getSourceName())) {
            eb.setImage(track.getInfo().getArtworkUrl());
         } else if ("youtube".equals(track.getInfo().getSourceName()) && this.playerManager.isNpImages()) {
            eb.setThumbnail(track.getInfo().getArtworkUrl());
         } else {
            eb.setImage("https://vinylgif.com/gifs/201412/stevie-wonder-sir-duke-vinyl.gif");
         }

         ActionRow row1 = ActionRow.of(
             Button.secondary("music:pause", Emoji.fromFormatted("⏯️")),
             Button.secondary("music:skip", Emoji.fromFormatted("⏭️")),
             Button.secondary("music:mute", isMuted ? Emoji.fromFormatted("🔊") : Emoji.fromFormatted("🔇")),
             Button.secondary("music:queue", Emoji.fromFormatted("📜")),
             Button.secondary("music:lyrics", Emoji.fromFormatted("📖"))
         );

         ActionRow row2 = ActionRow.of(
             Button.secondary("music:shuffle", Emoji.fromFormatted("🔀")),
             Button.secondary("music:repeat", Emoji.fromFormatted("🔁")),
             Button.secondary("music:effects", Emoji.fromFormatted("✨"))
         );

         return new MessageCreateBuilder()
            .setEmbeds(eb.build())
            .setComponents(row1, row2)
            .build();
      } else {
         return this.getNoMusicPlaying(jda, this.playerManager.getBot().getSettingsManager().getLanguageService(this.guildId));
      }
   }

   public MessageCreateData getNoMusicPlaying(JDA jda, LanguageService languageService) {
      Guild guild = this.guild(jda);
      return ((MessageCreateBuilder)((MessageCreateBuilder)new MessageCreateBuilder()
               .setContent(
                  FormatUtil.filter(
                     this.playerManager.getSuccessEmoji()
                        + " "
                        + languageService.getMessage("music.nowplaying2", new Object[]{this.playerManager.getSuccessEmoji()})
                  )
               ))
            .setEmbeds(
               new MessageEmbed[]{
                  new EmbedBuilder()
                     .setTitle(languageService.getMessage("music.nothingplaying"))
                     .setDescription(
                        "⏹ "
                           + FormatUtil.progressBar(-1.0)
                           + " "
                           + FormatUtil.volumeIcon(this.getAudioPlayer().isPresent() ? this.getAudioPlayer().get().getVolume() : 100)
                     )
                     .setColor(guild.getSelfMember().getColor())
                     .build()
               }
            ))
         .build();
   }

   public String getTopicFormat(JDA jda, LanguageService languageService) {
      if (this.isMusicPlaying(jda)) {
         long userid = 0L;
         LavalinkPlayer audioPlayer = this.getAudioPlayer().get();

         try {
            userid = this.getRequestMetadata().owner();
         } catch (IOException var8) {
            throw new RuntimeException(var8);
         }

         dev.arbjerg.lavalink.client.player.Track track = audioPlayer.getTrack();
         String title = track.getInfo().getTitle();
         if (title == null || title.equals("Titulo desconocido")) {
            title = track.getInfo().getUri();
         }

         return "**"
            + title
            + "** ["
            + (userid == 0L ? languageService.getMessage("music.autoplay") : "<@" + userid + ">")
            + "]\n"
            + (audioPlayer.getPaused() ? "⏸" : "▶")
            + " ["
            + FormatUtil.formatTime(track.getInfo().getLength())
            + "] "
            + FormatUtil.volumeIcon(audioPlayer.getVolume());
      } else {
         return languageService.getMessage(
            "music.no.music.playing",
            new Object[]{"⏹", FormatUtil.volumeIcon(this.getAudioPlayer().isPresent() ? this.getAudioPlayer().get().getVolume() : 100)}
         );
      }
   }

   public void onTrackEnd(TrackEndEvent event) {
      dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason = event.getEndReason();
      if (endReason == AudioTrackEndReason.REPLACED || endReason == AudioTrackEndReason.CLEANUP) {
         return;
      }

      RepeatMode repeatMode = this.playerManager.getBot().getSettingsManager().getSettings(this.guildId).getRepeatMode();
      dev.arbjerg.lavalink.client.player.Track track = event.getTrack();
      if (endReason == AudioTrackEndReason.FINISHED && repeatMode != RepeatMode.OFF) {
         QueuedTrack clone = new QueuedTrack(track.makeClone(), track.getUserData(RequestMetadata.class));
         if (repeatMode == RepeatMode.ALL) {
            this.queueManager.addToTrackQueue(clone);
         } else {
            this.queueManager.addToTrackQueueAt(0, clone);
         }
      }

      if (this.queueManager.getQueue().isEmpty()) {
         if (!this.playFromDefault()) {
            LanguageService lang = this.playerManager.getBot().getSettingsManager().getLanguageService(this.guildId);
            this.playerManager.getNowplayingHandler().onTrackUpdate(this.guildId, null, this, lang);
            if (!this.playerManager.isStayInChannel()) {
                this.playerManager.destroyPlayer(this.guildId);
            }
         }
      } else {
         QueuedTrack qt = this.queueManager.getQueue().pull();
         this.getAudioPlayer().ifPresent(player -> player.setPaused(false).setTrack(qt.getTrack()).subscribe());
      }
   }

   public void onWebSocketClosed(WebSocketClosedEvent event) {
      if (event.getGuildId() == this.guildId) {
         log.info("WebSocket closed: {} - {}", event.getCode(), event.getReason());
         if (!this.playFromDefault()) {
            LanguageService lang = this.playerManager.getBot().getSettingsManager().getLanguageService(this.guildId);
            this.playerManager.getNowplayingHandler().onTrackUpdate(this.guildId, null, this, lang);
            if (!this.playerManager.isStayInChannel()
               && !this.playerManager.getBot().getJDA().getGuildById(this.guildId).getSelfMember().getVoiceState().inAudioChannel()) {
               this.playerManager.getBot().closeAudioConnection(this.guildId);
            }

            this.playerManager.getNowplayingHandler().disableLastNPMessage(this.guildId);
            this.queueManager.getQueue().clear();
            this.playTrack(null);
         }
      }
   }

   public boolean playFromDefault() {
      if (!this.defaultQueue.isEmpty()) {
         this.getAudioPlayer().ifPresent(player -> player.setPaused(false).setTrack(this.defaultQueue.remove(0)).subscribe());
         return true;
      } else {
         Server settingsTEST = this.playerManager.getBot().getSettingsManager().getSettings(this.guildId);
         if (settingsTEST != null && settingsTEST.getDefaultPlaylist() != null) {
            PlaylistLoader.Playlist pl = this.playerManager.getBot().getPlaylistLoader().getPlaylist(settingsTEST.getDefaultPlaylist());
            if (pl != null && !pl.getItems().isEmpty()) {
               pl.loadTracks(this.getLink(), at -> {
                  this.getAudioPlayer().ifPresent(player -> {
                     if (player.getTrack() == null) {
                        player.setPaused(false).setTrack(at).subscribe();
                     } else {
                        this.defaultQueue.add(at);
                     }
                  });
               }, () -> {
                  if (pl.getTracks().isEmpty() && !this.playerManager.isStayInChannel()) {
                     this.playerManager.getBot().closeAudioConnection(this.guildId);
                  }
               });
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private Guild guild(JDA jda) {
      return jda.getGuildById(this.guildId);
   }

   public void stopAndClear() {
      this.queueManager.clear();
      this.defaultQueue.clear();
      this.getAudioPlayer().ifPresent(LavalinkPlayer::stopTrack);
      this.playerManager.getNowplayingHandler().clearLastNPMessage(this.guildId);
   }

   public void stopAndClearSync() {
      this.queueManager.clear();
      this.defaultQueue.clear();
      this.getAudioPlayer().ifPresent(LavalinkPlayer::stopTrack);
      try {
          this.getLink().destroy();
      } catch (Exception ignored) {}
      this.playerManager.getNowplayingHandler().clearLastNPMessageSync(this.guildId);
   }

   public void toggleMute() {
      this.getAudioPlayer().ifPresent(player -> {
         if (this.isMuted) {
            player.setVolume(this.previousVolume).subscribe();
            this.isMuted = false;
         } else {
            this.previousVolume = player.getVolume();
            player.setVolume(0).subscribe();
            this.isMuted = true;
         }
      });
   }

   public boolean isMuted() {
      return this.isMuted;
   }

   public void setEffect(String type) {
      this.getAudioPlayer().ifPresent(player -> {
         FilterBuilder builder = new FilterBuilder();
          switch (type) {
              case "effect_bassboost":
                  builder.setEqualizer(java.util.List.of(
                      new Band(0, 0.2f),
                      new Band(1, 0.15f),
                      new Band(2, 0.1f),
                      new Band(3, 0.05f)
                  ));
                  break;
              case "effect_nightcore":
                  builder.setTimescale(new Timescale(1.2f, 1.2f, 1.0f));
                  break;
              case "effect_vaporwave":
                  builder.setTimescale(new Timescale(0.85f, 0.8f, 1.0f));
                  break;
              case "effect_karaoke":
                  builder.setKaraoke(new Karaoke(1.0f, 1.0f, 220.0f, 100.0f));
                  break;
              case "effect_distortion":
                  builder.setDistortion(new Distortion(0.5f, 1.0f, 0.5f, 1.0f, 0.5f, 1.0f, 0.5f, 1.0f));
                  break;
              case "effect_none":
              default:
                  break;
          }
          this.currentEffect = type;
          player.setFilters(builder.build()).subscribe();
      });
   }

   public MessageEditData disableButtons(Message message) {
      MessageEditBuilder builder = MessageEditBuilder.fromMessage(message);
      List<ActionRow> rows = message.getComponents().stream()
         .filter(ActionRow.class::isInstance)
         .map(ActionRow.class::cast)
         .map(row -> ActionRow.of(row.getComponents().stream()
            .map(item -> item instanceof Button ? ((Button) item).asDisabled() : item)
            .map(ActionRowChildComponent.class::cast)
            .toList()))
         .toList();
      return builder.setComponents(rows).build();
   }

   /**
    * Checks if there are available Lavalink nodes before attempting operations.
    * @return true if nodes are available, false otherwise
    */
   public boolean areNodesAvailable() {
      return this.playerManager.hasAvailableNodes();
   }

   /**
    * Safely attempts to get a link with node availability checking.
    * @return Optional containing the link if available, empty otherwise
    */
   public Optional<Link> getSafeLink() {
      if (!areNodesAvailable()) {
         log.warn("No available nodes for guild {}, cannot create link", this.guildId);
         return Optional.empty();
      }
      try {
         return Optional.of(this.getLink());
      } catch (IllegalStateException e) {
         log.error("Failed to get link for guild {} despite available nodes: {}", this.guildId, e.getMessage());
         return Optional.empty();
      }
   }

   public MessageCreateData getQueueMessage(JDA jda, int page, Guild guild) {
      List<QueuedTrack> list = this.queueManager.getQueue().getList();
      LanguageService lang = this.playerManager.getBot().getSettingsManager().getLanguageService(this.guildId);
      if (list.isEmpty()) {
         return null;
      } else {
         int maxPages = (int) Math.ceil(list.size() / 10.0);
         if (page < 1) page = 1;
         if (page > maxPages) page = maxPages;

         int start = (page - 1) * 10;
         int end = Math.min(start + 10, list.size());

         EmbedBuilder eb = new EmbedBuilder()
            .setTitle(lang.getMessage("command.music.queue.title"))
            .setColor(guild.getSelfMember().getColor());

         StringBuilder sb = new StringBuilder();
         for (int i = start; i < end; i++) {
            sb.append(i + 1).append(". ").append(list.get(i).toString()).append("\n");
         }
         eb.setDescription(sb.toString());

         long totalDuration = list.stream().mapToLong(t -> t.getTrack().getInfo().getLength()).sum();
         RepeatMode repeatMode = this.playerManager.getBot().getSettingsManager().getSettings(this.guildId).getRepeatMode();

         eb.setFooter(FormatUtil.formatLocaleWithoutFilter(lang, "music.queue.footer", 
             list.size(), 
             FormatUtil.formatTime(totalDuration), 
             page, 
             maxPages,
             lang.getMessage("music.repeat." + repeatMode.getKey())), null);

         Button prev = Button.primary("music:queue:page:" + (page - 1), Emoji.fromFormatted("⬅️")).withDisabled(page <= 1);
         Button next = Button.primary("music:queue:page:" + (page + 1), Emoji.fromFormatted("➡️")).withDisabled(page >= maxPages);
         Button clear = Button.danger("music:queue:clear", Emoji.fromFormatted("🗑️"));

         return new MessageCreateBuilder()
            .setEmbeds(eb.build())
            .setComponents(ActionRow.of(prev, next, clear))
            .build();
      }
   }

   @Generated
   public Set<String> getVotes() {
      return this.votes;
   }

   @Generated
   public QueueManager getQueueManager() {
      return this.queueManager;
   }
}
