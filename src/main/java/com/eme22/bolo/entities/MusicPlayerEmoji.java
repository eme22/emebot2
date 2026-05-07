package com.eme22.bolo.entities;

public enum MusicPlayerEmoji {
   MUTE("\ud83d\udd07"),
   NEXT("⏭"),
   PLAYORPAUSE("⏯"),
   LYRICS("\ud83c\udfb5"),
   QUEUE("\ud83d\udcc3");

   private final String emoji;

   private MusicPlayerEmoji(String emoji) {
      this.emoji = emoji;
   }

   @Override
   public String toString() {
      return this.emoji;
   }

   public static MusicPlayerEmoji isEmojiValid(String text) {
      switch (text) {
         case "\ud83d\udd07":
            return MUTE;
         case "⏭":
            return NEXT;
         case "⏯":
            return PLAYORPAUSE;
         case "\ud83c\udfb5":
            return LYRICS;
         case "\ud83d\udcc3":
            return QUEUE;
         default:
            return null;
      }
   }
}
