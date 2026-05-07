package com.eme22.bolo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Generated;
import org.hibernate.Hibernate;

@Entity(name = "embot_server_birthday_detail")
public class Birthday {
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "birthday_id", nullable = false)
   private Long id;
   @Column(name = "birthday_message")
   private String message;
   @Column(name = "birthday_date")
   private LocalDate date;
   @Column(name = "birthday_userid")
   private Long user;
   @Column(name = "birthday_serverid")
   private Long server;
   @Column(name = "birthday_enabled", columnDefinition = "boolean default true")
   private boolean enabled;

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && Hibernate.getClass(this) == Hibernate.getClass(o)) {
         Birthday birthday = (Birthday) o;
         return this.getId() != null && Objects.equals(this.getId(), birthday.getId());
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
   public LocalDate getDate() {
      return this.date;
   }

   @Generated
   public Long getUser() {
      return this.user;
   }

   @Generated
   public Long getServer() {
      return this.server;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
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
   public void setDate(final LocalDate date) {
      this.date = date;
   }

   @Generated
   public void setUser(final Long user) {
      this.user = user;
   }

   @Generated
   public void setServer(final Long server) {
      this.server = server;
   }

   @Generated
   public void setEnabled(final boolean enabled) {
      this.enabled = enabled;
   }

   @Generated
   @Override
   public String toString() {
      return "Birthday(id="
            + this.getId()
            + ", message="
            + this.getMessage()
            + ", date="
            + this.getDate()
            + ", user="
            + this.getUser()
            + ", server="
            + this.getServer()
            + ", enabled="
            + this.isEnabled()
            + ")";
   }
}
