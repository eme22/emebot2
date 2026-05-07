package com.eme22.bolo.language;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import lombok.Generated;

public class LanguageService {
   private final ResourceBundle resourceBundle;
   private final Locale locale;
   private String sucessEmoji = "✅";
   private String errorEmoji = "❌";
   private String warningEmoji = "⚠️";

   public LanguageService(String languageCode, String sucessEmoji, String errorEmoji, String warningEmoji) {
      this.locale = Locale.of(languageCode);
      this.resourceBundle = ResourceBundle.getBundle("messages", this.locale);
      this.sucessEmoji = sucessEmoji;
      this.errorEmoji = errorEmoji;
      this.warningEmoji = warningEmoji;
   }

   public String getSuccessMessage(String key) {
      return this.sucessEmoji + " " + this.getMessage(key);
   }

   public String getSuccessMessage(String key, Object... args) {
      return this.sucessEmoji + " " + this.getMessage(key, args);
   }

   public String getErrorMessage(String key) {
      return this.errorEmoji + " " + this.getMessage(key);
   }

   public String getErrorMessage(String key, Object... args) {
      return this.errorEmoji + " " + this.getMessage(key, args);
   }

   public String getWarningMessage(String key) {
      return this.warningEmoji + " " + this.getMessage(key);
   }

   public String getWarningMessage(String key, Object... args) {
      return this.warningEmoji + " " + this.getMessage(key, args);
   }

   public String getMessage(String key) {
      try {
         return this.resourceBundle.getString(key);
      } catch (Exception e) {
         return key;
      }
   }

   public String getMessage(String key, Object... args) {
      try {
         MessageFormat messageFormat = new MessageFormat(this.resourceBundle.getString(key), this.locale);
         return messageFormat.format(args);
      } catch (Exception e) {
         return key;
      }
   }

   @Generated
   @Override
   public String toString() {
      return "LanguageService(locale="
         + this.getLocale()
         + ", sucessEmoji="
         + this.getSucessEmoji()
         + ", errorEmoji="
         + this.getErrorEmoji()
         + ", warningEmoji="
         + this.getWarningEmoji()
         + ")";
   }

   @Generated
   public Locale getLocale() {
      return this.locale;
   }

   @Generated
   public String getSucessEmoji() {
      return this.sucessEmoji;
   }

   @Generated
   public void setSucessEmoji(final String sucessEmoji) {
      this.sucessEmoji = sucessEmoji;
   }

   @Generated
   public String getErrorEmoji() {
      return this.errorEmoji;
   }

   @Generated
   public void setErrorEmoji(final String errorEmoji) {
      this.errorEmoji = errorEmoji;
   }

   @Generated
   public String getWarningEmoji() {
      return this.warningEmoji;
   }

   @Generated
   public void setWarningEmoji(final String warningEmoji) {
      this.warningEmoji = warningEmoji;
   }
}
