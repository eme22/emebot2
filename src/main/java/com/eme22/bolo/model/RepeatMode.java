package com.eme22.bolo.model;

import java.util.Objects;
import java.util.stream.Stream;
import lombok.Generated;

public enum RepeatMode {
    OFF("\ud83d\udeab", "off"),
    ALL("\ud83d\udd01", "all"),
    SINGLE("\ud83d\udd02", "single");

    private final String emoji;
    private final String key;

    private RepeatMode(String emoji, String key) {
       this.emoji = emoji;
       this.key = key;
    }

   public static RepeatMode of(String mode) {
      return Stream.of(values()).filter(p -> Objects.equals(p.getKey(), mode)).findFirst().orElseThrow(IllegalArgumentException::new);
   }

   @Generated
   public String getEmoji() {
      return this.emoji;
   }

   @Generated
   public String getKey() {
      return this.key;
   }

   @Deprecated
   public String getUserFriendlyName() {
       return this.key;
   }
}
