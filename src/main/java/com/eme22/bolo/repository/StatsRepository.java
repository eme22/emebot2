package com.eme22.bolo.repository;

import com.eme22.bolo.model.Stats;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class StatsRepository implements PanacheRepositoryBase<Stats, String> {

   @Transactional
   public int updateStat(String name) {
      return update("value = value + 1 where name = ?1", name);
   }

   @Transactional
   public int updateStat(String name, Long value) {
      return update("value = ?2 where name = ?1", value, name);
   }
}
