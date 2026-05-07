package com.eme22.bolo.repository;

import com.eme22.bolo.model.Birthday;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BirthdayRepository implements PanacheRepository<Birthday> {
   
   public List<Birthday> findByDate(int day, int month) {
       return list("day(date) = ?1 and month(date) = ?2 and enabled = true", day, month);
   }

   public List<Birthday> findByDateAndServer(int day, int month, long server) {
       return list("day(date) = ?1 and month(date) = ?2 and enabled = true and server = ?3", day, month, server);
   }

   public List<Birthday> findByServer(long idLong) {
       return list("server", idLong);
   }

}
