package com.eme22.bolo.image;

import com.eme22.bolo.model.MusicArtWork;
import com.eme22.bolo.repository.MusicArtWorkRepository;
import java.util.Optional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ArtworkImageService {
   private final MusicArtWorkRepository repository;

   @Inject
   public ArtworkImageService(MusicArtWorkRepository repository) {
      this.repository = repository;
   }

   @Transactional
   public Optional<MusicArtWork> getArtwork(String artist) {
      return this.repository.findByArtistIgnoreCase(artist);
   }

   @Transactional
   public MusicArtWork addArtWork(MusicArtWork artist) {
      this.repository.persist(artist);
      return artist;
   }

   @Transactional
   public void updateArtWork(MusicArtWork artist) {
      this.repository.updateUrlByArtistIgnoreCase(artist.getUrl(), artist.getArtist());
   }

   @Transactional
   public void removeMusicArtwork(String artist) {
      this.repository.deleteByArtistIgnoreCase(artist);
   }
}

