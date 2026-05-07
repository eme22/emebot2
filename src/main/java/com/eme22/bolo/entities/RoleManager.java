package com.eme22.bolo.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.HashMap;
import lombok.Generated;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"id", "message", "emojilist"})
public class RoleManager {
   @JsonProperty("id")
   private long id;
   @JsonProperty("message")
   private String message;
   @JsonProperty("emojilist")
   private HashMap<String, String> emoji;
   @JsonProperty("toggled")
   private boolean toggled;

   @Generated
   public RoleManager(final long id, final String message, final HashMap<String, String> emoji, final boolean toggled) {
      this.id = id;
      this.message = message;
      this.emoji = emoji;
      this.toggled = toggled;
   }

   @Generated
   public RoleManager withId(final long id) {
      return this.id == id ? this : new RoleManager(id, this.message, this.emoji, this.toggled);
   }

   @Generated
   public RoleManager withMessage(final String message) {
      return this.message == message ? this : new RoleManager(this.id, message, this.emoji, this.toggled);
   }

   @Generated
   public RoleManager withEmoji(final HashMap<String, String> emoji) {
      return this.emoji == emoji ? this : new RoleManager(this.id, this.message, emoji, this.toggled);
   }

   @Generated
   public RoleManager withToggled(final boolean toggled) {
      return this.toggled == toggled ? this : new RoleManager(this.id, this.message, this.emoji, toggled);
   }

   @Generated
   public long getId() {
      return this.id;
   }

   @Generated
   public String getMessage() {
      return this.message;
   }

   @Generated
   public HashMap<String, String> getEmoji() {
      return this.emoji;
   }

   @Generated
   public boolean isToggled() {
      return this.toggled;
   }

   @JsonProperty("id")
   @Generated
   public void setId(final long id) {
      this.id = id;
   }

   @JsonProperty("message")
   @Generated
   public void setMessage(final String message) {
      this.message = message;
   }

   @JsonProperty("emojilist")
   @Generated
   public void setEmoji(final HashMap<String, String> emoji) {
      this.emoji = emoji;
   }

   @JsonProperty("toggled")
   @Generated
   public void setToggled(final boolean toggled) {
      this.toggled = toggled;
   }

   @Generated
   public RoleManager() {
   }

   @Generated
   @Override
   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof RoleManager other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.getId() != other.getId()) {
         return false;
      } else if (this.isToggled() != other.isToggled()) {
         return false;
      } else {
         Object this$message = this.getMessage();
         Object other$message = other.getMessage();
         if (this$message == null ? other$message == null : this$message.equals(other$message)) {
            Object this$emoji = this.getEmoji();
            Object other$emoji = other.getEmoji();
            return this$emoji == null ? other$emoji == null : this$emoji.equals(other$emoji);
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(final Object other) {
      return other instanceof RoleManager;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      long $id = this.getId();
      result = result * 59 + (int)($id >>> 32 ^ $id);
      result = result * 59 + (this.isToggled() ? 79 : 97);
      Object $message = this.getMessage();
      result = result * 59 + ($message == null ? 43 : $message.hashCode());
      Object $emoji = this.getEmoji();
      return result * 59 + ($emoji == null ? 43 : $emoji.hashCode());
   }
}
