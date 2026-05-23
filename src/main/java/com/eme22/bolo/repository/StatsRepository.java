package com.eme22.bolo.repository;

import com.eme22.bolo.model.Stats;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class StatsRepository implements PanacheRepositoryBase<Stats, String> {

   private void ensureExists(String name) {
      if (findById(name) == null) {
         persist(new Stats(name, 0L));
      }
   }

   @Transactional
   public int updateStat(String name) {
      ensureExists(name);
      return update("value = value + 1 where name = ?1", name);
   }

   @Transactional
   public int updateStat(String name, Long value) {
      ensureExists(name);
      return update("value = ?2 where name = ?1", value, name);
   }

   @Transactional
   public int incrementStat(String name, long diff) {
      ensureExists(name);
      return update("value = value + ?2 where name = ?1", name, diff);
   }
}
