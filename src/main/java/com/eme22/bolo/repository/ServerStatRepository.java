package com.eme22.bolo.repository;

import com.eme22.bolo.model.ServerStat;
import com.eme22.bolo.model.ServerStatId;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ServerStatRepository implements PanacheRepositoryBase<ServerStat, ServerStatId> {

   private void ensureExists(Long serverId, String statName) {
      if (findById(new ServerStatId(serverId, statName)) == null) {
         persist(new ServerStat(serverId, statName, 0L));
      }
   }

   @Transactional
   public void incrementServerStat(Long serverId, String name, long diff) {
      ensureExists(serverId, name);
      update("value = value + ?3 where serverId = ?1 and statName = ?2", serverId, name, diff);
   }
}
