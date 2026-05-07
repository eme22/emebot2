package com.eme22.bolo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Date;
import java.util.Objects;
import lombok.Generated;
import org.hibernate.Hibernate;

@Entity(name = "embot_user")
public class User {
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "user_id", nullable = false)
   private Long id;
   @Column(name = "user_name", nullable = false)
   String name;
   @Column(name = "user_role", nullable = false)
   @Enumerated(EnumType.STRING)
   UserRole role;
   @Column(name = "user_lastlogindate", nullable = false)
   Date lastLogin;
   @Column(name = "user_registerdate", nullable = false)
   Date registerDate;

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && Hibernate.getClass(this) == Hibernate.getClass(o)) {
         User user = (User) o;
         return this.getId() != null && Objects.equals(this.getId(), user.getId());
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
   public String getName() {
      return this.name;
   }

   @Generated
   public UserRole getRole() {
      return this.role;
   }

   @Generated
   public Date getLastLogin() {
      return this.lastLogin;
   }

   @Generated
   public Date getRegisterDate() {
      return this.registerDate;
   }

   @Generated
   public void setId(final Long id) {
      this.id = id;
   }

   @Generated
   public void setName(final String name) {
      this.name = name;
   }

   @Generated
   public void setRole(final UserRole role) {
      this.role = role;
   }

   @Generated
   public void setLastLogin(final Date lastLogin) {
      this.lastLogin = lastLogin;
   }

   @Generated
   public void setRegisterDate(final Date registerDate) {
      this.registerDate = registerDate;
   }

   @Generated
   @Override
   public String toString() {
      return "User(id="
            + this.getId()
            + ", name="
            + this.getName()
            + ", role="
            + this.getRole()
            + ", lastLogin="
            + this.getLastLogin()
            + ", registerDate="
            + this.getRegisterDate()
            + ")";
   }
}
