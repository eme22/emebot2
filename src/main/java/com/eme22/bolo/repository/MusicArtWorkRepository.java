package com.eme22.bolo.repository;

import com.eme22.bolo.model.MusicArtWork;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import java.util.Optional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MusicArtWorkRepository implements PanacheRepository<MusicArtWork> {
   
   @Transactional
   public int updateUrlByArtistIgnoreCase(String url, String artist) {
       return update("url = ?1 where upper(artist) = upper(?2)", url, artist);
   }

   @Transactional
   public long deleteByArtistIgnoreCase(String artist) {
       return delete("upper(artist) = upper(?1)", artist);
   }

   @Transactional
   public Optional<MusicArtWork> findByArtistIgnoreCase(String artist) {
       return find("upper(artist) = upper(?1)", artist).firstResultOptional();
   }
}
