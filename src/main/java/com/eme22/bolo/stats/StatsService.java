package com.eme22.bolo.stats;

import com.eme22.bolo.model.ServerStats;
import com.eme22.bolo.model.Stats;
import com.eme22.bolo.repository.ServerStatsRepository;
import com.eme22.bolo.repository.StatsRepository;
import java.util.Optional;
import java.util.List;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class StatsService {
   private final StatsRepository statsRepository;
   private final ServerStatsRepository serverStatsRepository;

   @Inject
   public StatsService(StatsRepository statsRepository, ServerStatsRepository serverStatsRepository) {
      this.statsRepository = statsRepository;
      this.serverStatsRepository = serverStatsRepository;
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
      return this.statsRepository.listAll();
   }

   @Transactional
   public void updateCommandsUsed(Long guildId) {
      try {
         this.serverStatsRepository.updateCommandsUsedById(guildId);
         this.statsRepository.updateStat("COMMANDS_USED");
      } catch (Exception var3) {
      }
   }

   @Transactional
   public void updateImagesSend(Long guildId) {
      try {
         this.serverStatsRepository.updateImagesSendById(guildId);
         this.statsRepository.updateStat("IMAGES_SEND");
      } catch (Exception var3) {
      }
   }

   @Transactional
   public void updateMemesSend(Long guildId) {
      try {
         this.serverStatsRepository.updateMemesSendById(guildId);
         this.statsRepository.updateStat("MEMES_SEND");
      } catch (Exception var3) {
      }
   }

   @Transactional
   public void updateSongsPlayed(Long guildId) {
      try {
         this.serverStatsRepository.updateSongsPlayedById(guildId);
         this.statsRepository.updateStat("SONGS_PLAYED");
      } catch (Exception var3) {
      }
   }

   @Transactional
   public void updateAnals(Long guildId) {
      try {
         this.serverStatsRepository.updateAnalById(guildId);
         this.statsRepository.updateStat("ANAL");
      } catch (Exception var3) {
      }
   }

   @Transactional
   public void updateKisses(Long guildId) {
      try {
         this.serverStatsRepository.updateKissesById(guildId);
         this.statsRepository.updateStat("KISS");
      } catch (Exception var3) {
      }
   }

   @Transactional
   public void updateSlaps(Long guildId) {
      try {
         this.serverStatsRepository.updateSlapsById(guildId);
         this.statsRepository.updateStat("SLAPS");
      } catch (Exception var3) {
      }
   }

   @Transactional
   public void updatePokes(Long guildId) {
      try {
         this.serverStatsRepository.updatePokeById(guildId);
         this.statsRepository.updateStat("POKES");
      } catch (Exception var3) {
      }
   }

   @Transactional
   public void updateBites(Long guildId) {
      try {
         this.serverStatsRepository.updateBiteById(guildId);
         this.statsRepository.updateStat("BITES");
      } catch (Exception var3) {
      }
   }

   @Transactional
   public void updateLicks(Long guildId) {
      try {
         this.serverStatsRepository.updateLickById(guildId);
         this.statsRepository.updateStat("LICKS");
      } catch (Exception var3) {
      }
   }

   @Transactional
   public void updateFucks(Long guildId) {
      try {
         this.serverStatsRepository.updateFuckById(guildId);
         this.statsRepository.updateStat("FUCKS");
      } catch (Exception var3) {
      }
   }

   @Transactional
   public void updateCums(Long guildId) {
      try {
         this.serverStatsRepository.updateCumById(guildId);
         this.statsRepository.updateStat("CUMS");
      } catch (Exception var3) {
      }
   }

   @Transactional
   public Optional<ServerStats> getServerStat(Long guildId) {
      return this.serverStatsRepository.findByIdOptional(guildId);
   }
}

