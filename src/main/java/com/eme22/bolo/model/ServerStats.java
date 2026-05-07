package com.eme22.bolo.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Generated;

@Entity(name = "embot_server_stats")
@RegisterForReflection
public class ServerStats {
   @Id
   @Column(name = "server_id", nullable = false)
   private Long id;
   @Column(name = "server_commands", nullable = false)
   private Long commandsUsed;
   @Column(name = "server_images", nullable = false)
   private Long imagesSend;
   @Column(name = "server_memes", nullable = false)
   private Long memesSend;
   @Column(name = "server_songs", nullable = false)
   private Long songsPlayed;
   @Column(name = "server_anal", nullable = false)
   private Long anal;
   @Column(name = "server_kisses", nullable = false)
   private Long kisses;
   @Column(name = "server_slaps", nullable = false)
   private Long slaps;
   @Column(name = "server_poke", nullable = false)
   private Long poke;
   @Column(name = "server_bite", nullable = false)
   private Long bite;
   @Column(name = "server_lick", nullable = false)
   private Long lick;
   @Column(name = "server_fuck", nullable = false)
   private Long fuck;
   @Column(name = "server_cum", nullable = false)
   private Long cum;

   @Generated
   public static ServerStats.ServerStatsBuilder builder() {
      return new ServerStats.ServerStatsBuilder();
   }

   @Generated
   public Long getId() {
      return this.id;
   }

   @Generated
   public Long getCommandsUsed() {
      return this.commandsUsed;
   }

   @Generated
   public Long getImagesSend() {
      return this.imagesSend;
   }

   @Generated
   public Long getMemesSend() {
      return this.memesSend;
   }

   @Generated
   public Long getSongsPlayed() {
      return this.songsPlayed;
   }

   @Generated
   public Long getAnal() {
      return this.anal;
   }

   @Generated
   public Long getKisses() {
      return this.kisses;
   }

   @Generated
   public Long getSlaps() {
      return this.slaps;
   }

   @Generated
   public Long getPoke() {
      return this.poke;
   }

   @Generated
   public Long getBite() {
      return this.bite;
   }

   @Generated
   public Long getLick() {
      return this.lick;
   }

   @Generated
   public Long getFuck() {
      return this.fuck;
   }

   @Generated
   public Long getCum() {
      return this.cum;
   }

   @Generated
   public void setId(final Long id) {
      this.id = id;
   }

   @Generated
   public void setCommandsUsed(final Long commandsUsed) {
      this.commandsUsed = commandsUsed;
   }

   @Generated
   public void setImagesSend(final Long imagesSend) {
      this.imagesSend = imagesSend;
   }

   @Generated
   public void setMemesSend(final Long memesSend) {
      this.memesSend = memesSend;
   }

   @Generated
   public void setSongsPlayed(final Long songsPlayed) {
      this.songsPlayed = songsPlayed;
   }

   @Generated
   public void setAnal(final Long anal) {
      this.anal = anal;
   }

   @Generated
   public void setKisses(final Long kisses) {
      this.kisses = kisses;
   }

   @Generated
   public void setSlaps(final Long slaps) {
      this.slaps = slaps;
   }

   @Generated
   public void setPoke(final Long poke) {
      this.poke = poke;
   }

   @Generated
   public void setBite(final Long bite) {
      this.bite = bite;
   }

   @Generated
   public void setLick(final Long lick) {
      this.lick = lick;
   }

   @Generated
   public void setFuck(final Long fuck) {
      this.fuck = fuck;
   }

   @Generated
   public void setCum(final Long cum) {
      this.cum = cum;
   }

   @Generated
   @Override
   public String toString() {
      return "ServerStats(id="
            + this.getId()
            + ", commandsUsed="
            + this.getCommandsUsed()
            + ", imagesSend="
            + this.getImagesSend()
            + ", memesSend="
            + this.getMemesSend()
            + ", songsPlayed="
            + this.getSongsPlayed()
            + ", anal="
            + this.getAnal()
            + ", kisses="
            + this.getKisses()
            + ", slaps="
            + this.getSlaps()
            + ", poke="
            + this.getPoke()
            + ", bite="
            + this.getBite()
            + ", lick="
            + this.getLick()
            + ", fuck="
            + this.getFuck()
            + ", cum="
            + this.getCum()
            + ")";
   }

   @Generated
   public ServerStats() {
   }

   @Generated
   public ServerStats(
         final Long id,
         final Long commandsUsed,
         final Long imagesSend,
         final Long memesSend,
         final Long songsPlayed,
         final Long anal,
         final Long kisses,
         final Long slaps,
         final Long poke,
         final Long bite,
         final Long lick,
         final Long fuck,
         final Long cum) {
      this.id = id;
      this.commandsUsed = commandsUsed;
      this.imagesSend = imagesSend;
      this.memesSend = memesSend;
      this.songsPlayed = songsPlayed;
      this.anal = anal;
      this.kisses = kisses;
      this.slaps = slaps;
      this.poke = poke;
      this.bite = bite;
      this.lick = lick;
      this.fuck = fuck;
      this.cum = cum;
   }

   @Generated
   public static class ServerStatsBuilder {
      @Generated
      private Long id;
      @Generated
      private Long commandsUsed;
      @Generated
      private Long imagesSend;
      @Generated
      private Long memesSend;
      @Generated
      private Long songsPlayed;
      @Generated
      private Long anal;
      @Generated
      private Long kisses;
      @Generated
      private Long slaps;
      @Generated
      private Long poke;
      @Generated
      private Long bite;
      @Generated
      private Long lick;
      @Generated
      private Long fuck;
      @Generated
      private Long cum;

      @Generated
      ServerStatsBuilder() {
      }

      @Generated
      public ServerStats.ServerStatsBuilder id(final Long id) {
         this.id = id;
         return this;
      }

      @Generated
      public ServerStats.ServerStatsBuilder commandsUsed(final Long commandsUsed) {
         this.commandsUsed = commandsUsed;
         return this;
      }

      @Generated
      public ServerStats.ServerStatsBuilder imagesSend(final Long imagesSend) {
         this.imagesSend = imagesSend;
         return this;
      }

      @Generated
      public ServerStats.ServerStatsBuilder memesSend(final Long memesSend) {
         this.memesSend = memesSend;
         return this;
      }

      @Generated
      public ServerStats.ServerStatsBuilder songsPlayed(final Long songsPlayed) {
         this.songsPlayed = songsPlayed;
         return this;
      }

      @Generated
      public ServerStats.ServerStatsBuilder anal(final Long anal) {
         this.anal = anal;
         return this;
      }

      @Generated
      public ServerStats.ServerStatsBuilder kisses(final Long kisses) {
         this.kisses = kisses;
         return this;
      }

      @Generated
      public ServerStats.ServerStatsBuilder slaps(final Long slaps) {
         this.slaps = slaps;
         return this;
      }

      @Generated
      public ServerStats.ServerStatsBuilder poke(final Long poke) {
         this.poke = poke;
         return this;
      }

      @Generated
      public ServerStats.ServerStatsBuilder bite(final Long bite) {
         this.bite = bite;
         return this;
      }

      @Generated
      public ServerStats.ServerStatsBuilder lick(final Long lick) {
         this.lick = lick;
         return this;
      }

      @Generated
      public ServerStats.ServerStatsBuilder fuck(final Long fuck) {
         this.fuck = fuck;
         return this;
      }

      @Generated
      public ServerStats.ServerStatsBuilder cum(final Long cum) {
         this.cum = cum;
         return this;
      }

      @Generated
      public ServerStats build() {
         return new ServerStats(this.id, this.commandsUsed, this.imagesSend, this.memesSend, this.songsPlayed,
               this.anal, this.kisses, this.slaps, this.poke, this.bite, this.lick, this.fuck, this.cum);
      }

      @Generated
      @Override
      public String toString() {
         return "ServerStats.ServerStatsBuilder(id="
               + this.id
               + ", commandsUsed="
               + this.commandsUsed
               + ", imagesSend="
               + this.imagesSend
               + ", memesSend="
               + this.memesSend
               + ", songsPlayed="
               + this.songsPlayed
               + ", anal="
               + this.anal
               + ", kisses="
               + this.kisses
               + ", slaps="
               + this.slaps
               + ", poke="
               + this.poke
               + ", bite="
               + this.bite
               + ", lick="
               + this.lick
               + ", fuck="
               + this.fuck
               + ", cum="
               + this.cum
               + ")";
      }
   }
}
