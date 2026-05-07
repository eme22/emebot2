package com.eme22.bolo.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Generated;

public class Birthday {
   @JsonProperty("message")
   private String message;
   @JsonProperty("date")
   private Date date;
   @JsonProperty("user")
   private long user;
   @JsonProperty("active")
   private boolean active;

   @Generated
   public Birthday(final String message, final Date date, final long user, final boolean active) {
      this.message = message;
      this.date = date;
      this.user = user;
      this.active = active;
   }

   @Generated
   public Birthday withMessage(final String message) {
      return this.message == message ? this : new Birthday(message, this.date, this.user, this.active);
   }

   @Generated
   public Birthday withDate(final Date date) {
      return this.date == date ? this : new Birthday(this.message, date, this.user, this.active);
   }

   @Generated
   public Birthday withUser(final long user) {
      return this.user == user ? this : new Birthday(this.message, this.date, user, this.active);
   }

   @Generated
   public Birthday withActive(final boolean active) {
      return this.active == active ? this : new Birthday(this.message, this.date, this.user, active);
   }

   @Generated
   public String getMessage() {
      return this.message;
   }

   @Generated
   public Date getDate() {
      return this.date;
   }

   @Generated
   public long getUser() {
      return this.user;
   }

   @Generated
   public boolean isActive() {
      return this.active;
   }

   @JsonProperty("message")
   @Generated
   public void setMessage(final String message) {
      this.message = message;
   }

   @JsonProperty("date")
   @Generated
   public void setDate(final Date date) {
      this.date = date;
   }

   @JsonProperty("user")
   @Generated
   public void setUser(final long user) {
      this.user = user;
   }

   @JsonProperty("active")
   @Generated
   public void setActive(final boolean active) {
      this.active = active;
   }

   @Generated
   public Birthday() {
   }
}
