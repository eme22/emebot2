package com.eme22.bolo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Generated;

@Entity(name = "embot_music_artworks")
@Table(indexes = { @Index(name = "idx_musicartwork_unq", columnList = "music_artworks_artist", unique = true) })
@NamedQueries({
      @NamedQuery(name = "MusicArtWork.updateUrlByArtistIgnoreCase", query = "update embot_music_artworks e set e.url = :url where upper(e.artist) = upper(:artist)") })
public class MusicArtWork {
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "music_artworks_id", nullable = false)
   private Long id;
   @Column(name = "music_artworks_artist")
   private String artist;
   @Column(name = "music_artworks_url")
   private String url;
   @Column(name = "music_artworks_user")
   private Long submitedBy;

   @Generated
   public Long getId() {
      return this.id;
   }

   @Generated
   public String getArtist() {
      return this.artist;
   }

   @Generated
   public String getUrl() {
      return this.url;
   }

   @Generated
   public Long getSubmitedBy() {
      return this.submitedBy;
   }

   @Generated
   public void setId(final Long id) {
      this.id = id;
   }

   @Generated
   public void setArtist(final String artist) {
      this.artist = artist;
   }

   @Generated
   public void setUrl(final String url) {
      this.url = url;
   }

   @Generated
   public void setSubmitedBy(final Long submitedBy) {
      this.submitedBy = submitedBy;
   }

   @Generated
   @Override
   public String toString() {
      return "MusicArtWork(id=" + this.getId() + ", artist=" + this.getArtist() + ", url=" + this.getUrl()
            + ", submitedBy=" + this.getSubmitedBy() + ")";
   }

   @Generated
   public MusicArtWork() {
   }

   @Generated
   public MusicArtWork(final Long id, final String artist, final String url, final Long submitedBy) {
      this.id = id;
      this.artist = artist;
      this.url = url;
      this.submitedBy = submitedBy;
   }
}
