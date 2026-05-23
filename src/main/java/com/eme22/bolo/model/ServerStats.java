package com.eme22.bolo.model;

public class ServerStats {
   private Long id;
   private Long commandsUsed = 0L;
   private Long imagesSend = 0L;
   private Long memesSend = 0L;
   private Long songsPlayed = 0L;
   private Long anal = 0L;
   private Long kisses = 0L;
   private Long slaps = 0L;
   private Long poke = 0L;
   private Long bite = 0L;
   private Long lick = 0L;
   private Long fuck = 0L;
   private Long cum = 0L;

   public ServerStats() {
   }

   public ServerStats(
         Long id,
         Long commandsUsed,
         Long imagesSend,
         Long memesSend,
         Long songsPlayed,
         Long anal,
         Long kisses,
         Long slaps,
         Long poke,
         Long bite,
         Long lick,
         Long fuck,
         Long cum) {
      this.id = id;
      this.commandsUsed = commandsUsed != null ? commandsUsed : 0L;
      this.imagesSend = imagesSend != null ? imagesSend : 0L;
      this.memesSend = memesSend != null ? memesSend : 0L;
      this.songsPlayed = songsPlayed != null ? songsPlayed : 0L;
      this.anal = anal != null ? anal : 0L;
      this.kisses = kisses != null ? kisses : 0L;
      this.slaps = slaps != null ? slaps : 0L;
      this.poke = poke != null ? poke : 0L;
      this.bite = bite != null ? bite : 0L;
      this.lick = lick != null ? lick : 0L;
      this.fuck = fuck != null ? fuck : 0L;
      this.cum = cum != null ? cum : 0L;
   }

   public Long getId() {
      return this.id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public Long getCommandsUsed() {
      return this.commandsUsed;
   }

   public void setCommandsUsed(Long commandsUsed) {
      this.commandsUsed = commandsUsed;
   }

   public Long getImagesSend() {
      return this.imagesSend;
   }

   public void setImagesSend(Long imagesSend) {
      this.imagesSend = imagesSend;
   }

   public Long getMemesSend() {
      return this.memesSend;
   }

   public void setMemesSend(Long memesSend) {
      this.memesSend = memesSend;
   }

   public Long getSongsPlayed() {
      return this.songsPlayed;
   }

   public void setSongsPlayed(Long songsPlayed) {
      this.songsPlayed = songsPlayed;
   }

   public Long getAnal() {
      return this.anal;
   }

   public void setAnal(Long anal) {
      this.anal = anal;
   }

   public Long getKisses() {
      return this.kisses;
   }

   public void setKisses(Long kisses) {
      this.kisses = kisses;
   }

   public Long getSlaps() {
      return this.slaps;
   }

   public void setSlaps(Long slaps) {
      this.slaps = slaps;
   }

   public Long getPoke() {
      return this.poke;
   }

   public void setPoke(Long poke) {
      this.poke = poke;
   }

   public Long getBite() {
      return this.bite;
   }

   public void setBite(Long bite) {
      this.bite = bite;
   }

   public Long getLick() {
      return this.lick;
   }

   public void setLick(Long lick) {
      this.lick = lick;
   }

   public Long getFuck() {
      return this.fuck;
   }

   public void setFuck(Long fuck) {
      this.fuck = fuck;
   }

   public Long getCum() {
      return this.cum;
   }

   public void setCum(Long cum) {
      this.cum = cum;
   }

   public static ServerStatsBuilder builder() {
      return new ServerStatsBuilder();
   }

   public static class ServerStatsBuilder {
      private Long id;
      private Long commandsUsed = 0L;
      private Long imagesSend = 0L;
      private Long memesSend = 0L;
      private Long songsPlayed = 0L;
      private Long anal = 0L;
      private Long kisses = 0L;
      private Long slaps = 0L;
      private Long poke = 0L;
      private Long bite = 0L;
      private Long lick = 0L;
      private Long fuck = 0L;
      private Long cum = 0L;

      ServerStatsBuilder() {
      }

      public ServerStatsBuilder id(Long id) {
         this.id = id;
         return this;
      }

      public ServerStatsBuilder commandsUsed(Long commandsUsed) {
         this.commandsUsed = commandsUsed;
         return this;
      }

      public ServerStatsBuilder imagesSend(Long imagesSend) {
         this.imagesSend = imagesSend;
         return this;
      }

      public ServerStatsBuilder memesSend(Long memesSend) {
         this.memesSend = memesSend;
         return this;
      }

      public ServerStatsBuilder songsPlayed(Long songsPlayed) {
         this.songsPlayed = songsPlayed;
         return this;
      }

      public ServerStatsBuilder anal(Long anal) {
         this.anal = anal;
         return this;
      }

      public ServerStatsBuilder kisses(Long kisses) {
         this.kisses = kisses;
         return this;
      }

      public ServerStatsBuilder slaps(Long slaps) {
         this.slaps = slaps;
         return this;
      }

      public ServerStatsBuilder poke(Long poke) {
         this.poke = poke;
         return this;
      }

      public ServerStatsBuilder bite(Long bite) {
         this.bite = bite;
         return this;
      }

      public ServerStatsBuilder lick(Long lick) {
         this.lick = lick;
         return this;
      }

      public ServerStatsBuilder fuck(Long fuck) {
         this.fuck = fuck;
         return this;
      }

      public ServerStatsBuilder cum(Long cum) {
         this.cum = cum;
         return this;
      }

      public ServerStats build() {
         return new ServerStats(id, commandsUsed, imagesSend, memesSend, songsPlayed, anal, kisses, slaps, poke, bite, lick, fuck, cum);
      }
   }
}
