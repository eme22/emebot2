package com.eme22.bolo.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Generated;

@Entity(name = "embot_stats")
@RegisterForReflection
public class Stats {
   @Id
   @Column(name = "stat_name", nullable = false)
   private String name;
   @Column(name = "stat_value", nullable = false)
   private Long value;

   @Generated
   public static Stats.StatsBuilder builder() {
      return new Stats.StatsBuilder();
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public Long getValue() {
      return this.value;
   }

   @Generated
   public void setName(final String name) {
      this.name = name;
   }

   @Generated
   public void setValue(final Long value) {
      this.value = value;
   }

   @Generated
   @Override
   public String toString() {
      return "Stats(name=" + this.getName() + ", value=" + this.getValue() + ")";
   }

   @Generated
   public Stats() {
   }

   @Generated
   public Stats(final String name, final Long value) {
      this.name = name;
      this.value = value;
   }

   @Generated
   public static class StatsBuilder {
      @Generated
      private String name;
      @Generated
      private Long value;

      @Generated
      StatsBuilder() {
      }

      @Generated
      public Stats.StatsBuilder name(final String name) {
         this.name = name;
         return this;
      }

      @Generated
      public Stats.StatsBuilder value(final Long value) {
         this.value = value;
         return this;
      }

      @Generated
      public Stats build() {
         return new Stats(this.name, this.value);
      }

      @Generated
      @Override
      public String toString() {
         return "Stats.StatsBuilder(name=" + this.name + ", value=" + this.value + ")";
      }
   }
}
