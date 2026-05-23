package com.eme22.bolo.stats;

import com.eme22.bolo.model.ServerStats;
import com.eme22.bolo.model.Stats;
import com.eme22.bolo.model.ServerStat;
import com.eme22.bolo.repository.StatsRepository;
import com.eme22.bolo.repository.ServerStatRepository;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.event.Observes;
import io.quarkus.runtime.ShutdownEvent;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class StatsService {
   private final StatsRepository statsRepository;
   private final ServerStatRepository serverStatRepository;

   private volatile ConcurrentHashMap<Long, ConcurrentHashMap<String, LongAdder>> serverBuffer = new ConcurrentHashMap<>();
   private volatile ConcurrentHashMap<String, LongAdder> globalBuffer = new ConcurrentHashMap<>();

   public ConcurrentHashMap<Long, ConcurrentHashMap<String, LongAdder>> getServerBuffer() {
      return this.serverBuffer;
   }

   @Inject
   public StatsService(StatsRepository statsRepository, ServerStatRepository serverStatRepository) {
      this.statsRepository = statsRepository;
      this.serverStatRepository = serverStatRepository;
   }

   public void increment(Long guildId, String statName) {
      if (guildId != null) {
         serverBuffer.computeIfAbsent(guildId, id -> new ConcurrentHashMap<>())
                     .computeIfAbsent(statName, name -> new LongAdder())
                     .increment();
      }
      globalBuffer.computeIfAbsent(statName, name -> new LongAdder()).increment();
   }

   @Scheduled(every = "30s")
   @Transactional
   public void flushStats() {
      ConcurrentHashMap<Long, ConcurrentHashMap<String, LongAdder>> serverSnapshot;
      ConcurrentHashMap<String, LongAdder> globalSnapshot;

      synchronized (this) {
         serverSnapshot = this.serverBuffer;
         this.serverBuffer = new ConcurrentHashMap<>();

         globalSnapshot = this.globalBuffer;
         this.globalBuffer = new ConcurrentHashMap<>();
      }

      if (serverSnapshot.isEmpty() && globalSnapshot.isEmpty()) {
         return;
      }

      log.info("Flushing stats to database...");

      // 1. Flush Global Stats
      for (Map.Entry<String, LongAdder> entry : globalSnapshot.entrySet()) {
         String name = entry.getKey();
         long diff = entry.getValue().sum();
         if (diff > 0) {
            try {
               statsRepository.incrementStat(name, diff);
            } catch (Exception e) {
               log.error("Failed to flush global stat {} with diff {}: {}", name, diff, e.getMessage(), e);
               globalBuffer.computeIfAbsent(name, n -> new LongAdder()).add(diff);
            }
         }
      }

      // 2. Flush Server Stats
      for (Map.Entry<Long, ConcurrentHashMap<String, LongAdder>> entry : serverSnapshot.entrySet()) {
         Long guildId = entry.getKey();
         ConcurrentHashMap<String, LongAdder> increments = entry.getValue();
         for (Map.Entry<String, LongAdder> statEntry : increments.entrySet()) {
            String statName = statEntry.getKey();
            long diff = statEntry.getValue().sum();
            if (diff > 0) {
               try {
                  serverStatRepository.incrementServerStat(guildId, statName, diff);
               } catch (Exception e) {
                  log.error("Failed to flush server stat {} for server {} with diff {}: {}", statName, guildId, diff, e.getMessage(), e);
                  serverBuffer.computeIfAbsent(guildId, id -> new ConcurrentHashMap<>())
                              .computeIfAbsent(statName, n -> new LongAdder())
                              .add(diff);
               }
            }
         }
      }
   }

   public void onStop(@Observes ShutdownEvent ev) {
      log.info("Application stopping. Flushing remaining stats...");
      flushStats();
   }

   @Transactional
   public void saveGlobalStat(String name, Long value) {
      this.statsRepository.persist(new Stats(name, value));
   }

   @Transactional
   public Stats getGlobalStat(String name) {
      return this.statsRepository.findByIdOptional(name).orElse(null);
   }

   @Transactional
   public List<Stats> getGlobalStats() {
      List<Stats> dbStats = this.statsRepository.listAll();
      List<Stats> realTimeStats = new java.util.ArrayList<>();
      for (Stats s : dbStats) {
         LongAdder adder = globalBuffer.get(s.getName());
         long bufferedVal = adder != null ? adder.sum() : 0L;
         realTimeStats.add(new Stats(s.getName(), s.getValue() + bufferedVal));
      }
      return realTimeStats;
   }

   @Transactional
   public Optional<ServerStats> getServerStat(Long guildId) {
      List<ServerStat> dbStats = this.serverStatRepository.list("serverId", guildId);
      ConcurrentHashMap<String, LongAdder> increments = serverBuffer.get(guildId);

      if (dbStats.isEmpty() && (increments == null || increments.isEmpty())) {
         return Optional.empty();
      }

      ServerStats dto = new ServerStats();
      dto.setId(guildId);

      for (ServerStat stat : dbStats) {
         String name = stat.getStatName();
         long value = stat.getValue();
         switch (name) {
            case "COMMANDS_USED" -> dto.setCommandsUsed(value);
            case "IMAGES_SEND" -> dto.setImagesSend(value);
            case "MEMES_SEND" -> dto.setMemesSend(value);
            case "SONGS_PLAYED" -> dto.setSongsPlayed(value);
            case "ANAL" -> dto.setAnal(value);
            case "KISS" -> dto.setKisses(value);
            case "SLAPS" -> dto.setSlaps(value);
            case "POKES" -> dto.setPoke(value);
            case "BITES" -> dto.setBite(value);
            case "LICKS" -> dto.setLick(value);
            case "FUCKS" -> dto.setFuck(value);
            case "CUMS" -> dto.setCum(value);
         }
      }

      if (increments != null) {
         dto.setCommandsUsed(dto.getCommandsUsed() + getBufferedValue(increments, "COMMANDS_USED"));
         dto.setImagesSend(dto.getImagesSend() + getBufferedValue(increments, "IMAGES_SEND"));
         dto.setMemesSend(dto.getMemesSend() + getBufferedValue(increments, "MEMES_SEND"));
         dto.setSongsPlayed(dto.getSongsPlayed() + getBufferedValue(increments, "SONGS_PLAYED"));
         dto.setAnal(dto.getAnal() + getBufferedValue(increments, "ANAL"));
         dto.setKisses(dto.getKisses() + getBufferedValue(increments, "KISS"));
         dto.setSlaps(dto.getSlaps() + getBufferedValue(increments, "SLAPS"));
         dto.setPoke(dto.getPoke() + getBufferedValue(increments, "POKES"));
         dto.setBite(dto.getBite() + getBufferedValue(increments, "BITES"));
         dto.setLick(dto.getLick() + getBufferedValue(increments, "LICKS"));
         dto.setFuck(dto.getFuck() + getBufferedValue(increments, "FUCKS"));
         dto.setCum(dto.getCum() + getBufferedValue(increments, "CUMS"));
      }

      return Optional.of(dto);
   }

   private long getBufferedValue(ConcurrentHashMap<String, LongAdder> increments, String key) {
      LongAdder adder = increments.get(key);
      return adder != null ? adder.sum() : 0L;
   }

   // Compatibility/Deprecated Wrappers
   @Deprecated
   public void updateCommandsUsed(Long guildId) {
      increment(guildId, "COMMANDS_USED");
   }

   @Deprecated
   public void updateImagesSend(Long guildId) {
      increment(guildId, "IMAGES_SEND");
   }

   @Deprecated
   public void updateMemesSend(Long guildId) {
      increment(guildId, "MEMES_SEND");
   }

   @Deprecated
   public void updateSongsPlayed(Long guildId) {
      increment(guildId, "SONGS_PLAYED");
   }

   @Deprecated
   public void updateAnals(Long guildId) {
      increment(guildId, "ANAL");
   }

   @Deprecated
   public void updateKisses(Long guildId) {
      increment(guildId, "KISS");
   }

   @Deprecated
   public void updateSlaps(Long guildId) {
      increment(guildId, "SLAPS");
   }

   @Deprecated
   public void updatePokes(Long guildId) {
      increment(guildId, "POKES");
   }

   @Deprecated
   public void updateBites(Long guildId) {
      increment(guildId, "BITES");
   }

   @Deprecated
   public void updateLicks(Long guildId) {
      increment(guildId, "LICKS");
   }

   @Deprecated
   public void updateFucks(Long guildId) {
      increment(guildId, "FUCKS");
   }

   @Deprecated
   public void updateCums(Long guildId) {
      increment(guildId, "CUMS");
   }
}
