package com.eme22.bolo.audio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.Generated;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

@Data
@RegisterForReflection
public class RequestMetadata {
   public static final RequestMetadata EMPTY = new RequestMetadata((RequestMetadata.UserInfo)null);
   @JsonProperty("user")
   public RequestMetadata.UserInfo user;

   @JsonCreator
   public RequestMetadata(@JsonProperty("user") RequestMetadata.UserInfo user) {
      this.user = user;
   }

   public RequestMetadata(User user, Guild guild) {
      this.user = user == null
         ? null
         : new RequestMetadata.UserInfo(user.getIdLong(), user.getName(), user.getDiscriminator(), user.getEffectiveAvatarUrl(), guild.getIdLong());
   }

   public RequestMetadata(User user, long guild) {
      this.user = user == null
         ? null
         : new RequestMetadata.UserInfo(user.getIdLong(), user.getName(), user.getDiscriminator(), user.getEffectiveAvatarUrl(), guild);
   }

   public RequestMetadata(long id, String username, String discrim, String avatar, Guild guild) {
      this.user = new RequestMetadata.UserInfo(id, username, discrim, avatar, guild.getIdLong());
   }

   public RequestMetadata(long id, String username, String discrim, String avatar, long guild) {
      this.user = new RequestMetadata.UserInfo(id, username, discrim, avatar, guild);
   }

   public long owner() {
      return this.user == null ? 0L : this.user.id;
   }

   public RequestMetadata.UserInfo user() {
      return this.user;
   }

   @Generated
   public RequestMetadata() {
   }

   @Data
   @RegisterForReflection
   public static class UserInfo {
      @JsonProperty("id")
      private long id;
      @JsonProperty("username")
      private String username;
      @JsonProperty("discrim")
      private String discrim;
      @JsonProperty("avatar")
      private String avatar;
      @JsonProperty("guild")
      private long guild;

      @JsonCreator
      public UserInfo(
         @JsonProperty("id") long id,
         @JsonProperty("username") String username,
         @JsonProperty("discrim") String discrim,
         @JsonProperty("avatar") String avatar,
         @JsonProperty("guild") long guild
      ) {
         this.id = id;
         this.username = username;
         this.discrim = discrim;
         this.avatar = avatar;
         this.guild = guild;
      }

      public long id() {
         return this.id;
      }

      public String username() {
         return this.username;
      }

      public String discrim() {
         return this.discrim;
      }

      public String avatar() {
         return this.avatar;
      }

      public long guild() {
         return this.guild;
      }

      @Generated
      public UserInfo() {
      }
   }
}
