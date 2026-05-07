package com.eme22.bolo.repository;

import com.eme22.bolo.model.ServerStats;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ServerStatsRepository implements PanacheRepository<ServerStats> {

   @Transactional
   public int updateImagesSendById(Long id) {
      return update("imagesSend = imagesSend + 1 where id = ?1", id);
   }

   @Transactional
   public int updateMemesSendById(Long id) {
      return update("memesSend = memesSend + 1 where id = ?1", id);
   }

   @Transactional
   public int updateSongsPlayedById(Long id) {
      return update("songsPlayed = songsPlayed + 1 where id = ?1", id);
   }

   @Transactional
   public int updateAnalById(Long id) {
      return update("anal = anal + 1 where id = ?1", id);
   }

   @Transactional
   public int updateKissesById(Long id) {
      return update("kisses = kisses + 1 where id = ?1", id);
   }

   @Transactional
   public int updateSlapsById(Long id) {
      return update("slaps = slaps + 1 where id = ?1", id);
   }

   @Transactional
   public void updateCommandsUsedById(Long id) {
      update("commandsUsed = commandsUsed + 1 where id = ?1", id);
   }

   @Transactional
   public int updatePokeById(Long id) {
      return update("poke = poke + 1 where id = ?1", id);
   }

   @Transactional
   public int updateBiteById(Long id) {
      return update("bite = bite + 1 where id = ?1", id);
   }

   @Transactional
   public int updateLickById(Long id) {
      return update("lick = lick + 1 where id = ?1", id);
   }

   @Transactional
   public int updateFuckById(Long id) {
      return update("fuck = fuck + 1 where id = ?1", id);
   }

   @Transactional
   public int updateCumById(Long id) {
      return update("cum = cum + 1 where id = ?1", id);
   }
}
