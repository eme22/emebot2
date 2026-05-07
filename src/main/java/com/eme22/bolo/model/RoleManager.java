package com.eme22.bolo.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.Map;
import java.util.Objects;
import lombok.Generated;
import org.hibernate.Hibernate;

@Entity(name = "embot_server_rolemanager_detail")
public class RoleManager {
   @Id
   @Column(name = "rolemanager_id")
   private Long id;
   @ElementCollection
   @CollectionTable(name = "embot_server_rolemanager_emoji")
   private Map<String, String> emoji;
   @Column(name = "rolemanager_toggledmode")
   private boolean toggled;

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && Hibernate.getClass(this) == Hibernate.getClass(o)) {
         RoleManager that = (RoleManager) o;
         return this.getId() != null && Objects.equals(this.getId(), that.getId());
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
   public Map<String, String> getEmoji() {
      return this.emoji;
   }

   @Generated
   public boolean isToggled() {
      return this.toggled;
   }

   @Generated
   public void setId(final Long id) {
      this.id = id;
   }

   @Generated
   public void setEmoji(final Map<String, String> emoji) {
      this.emoji = emoji;
   }

   @Generated
   public void setToggled(final boolean toggled) {
      this.toggled = toggled;
   }

   @Generated
   @Override
   public String toString() {
      return "RoleManager(id=" + this.getId() + ", emoji=" + this.getEmoji() + ", toggled=" + this.isToggled() + ")";
   }
}
