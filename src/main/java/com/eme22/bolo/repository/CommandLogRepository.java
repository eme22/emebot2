package com.eme22.bolo.repository;

import com.eme22.bolo.model.CommandLog;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommandLogRepository implements PanacheRepository<CommandLog> {
   
   public List<CommandLog> findByServer(long idLong) {
       return list("server", idLong);
   }

   public List<CommandLog> findByServerAndChannel(long idLong, long channel) {
       return list("server = ?1 and channel = ?2", idLong, channel);
   }

   public List<CommandLog> findByServerAndUser(long idLong, long user) {
       return list("server = ?1 and user = ?2", idLong, user);
   }

   public List<CommandLog> findByServerAndChannelAndUser(long idLong, long channel, long user) {
       return list("server = ?1 and channel = ?2 and user = ?3", idLong, channel, user);
   }
}
