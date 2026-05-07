package com.eme22.bolo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import lombok.Generated;
import org.hibernate.Hibernate;

@Entity(name = "embot_server_memeimage_detail")
public class MemeImage {
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "memeimage_id", nullable = false)
   private Long id;
   @Column(name = "memeimage_message")
   private String message;
   @Column(name = "memeimage_meme")
   private String meme;

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && Hibernate.getClass(this) == Hibernate.getClass(o)) {
         MemeImage memeImage = (MemeImage) o;
         return this.getId() != null && Objects.equals(this.getId(), memeImage.getId());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.getClass().hashCode();
   }

   @Generated
   public Long getId() {
      return this.id;
   }

   @Generated
   public String getMessage() {
      return this.message;
   }

   @Generated
   public String getMeme() {
      return this.meme;
   }

   @Generated
   public void setId(final Long id) {
      this.id = id;
   }

   @Generated
   public void setMessage(final String message) {
      this.message = message;
   }

   @Generated
   public void setMeme(final String meme) {
      this.meme = meme;
   }

   @Generated
   @Override
   public String toString() {
      return "MemeImage(id=" + this.getId() + ", message=" + this.getMessage() + ", meme=" + this.getMeme() + ")";
   }

   @Generated
   public MemeImage() {
   }

   @Generated
   public MemeImage(final Long id, final String message, final String meme) {
      this.id = id;
      this.message = message;
      this.meme = meme;
   }
}
