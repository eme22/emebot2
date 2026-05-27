package com.eme22.bolo.settings;

import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.model.RepeatMode;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.repository.ServerRepository;
import com.jagrosh.jdautilities.command.GuildSettingsManager;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import jakarta.transaction.Transactional;
import lombok.Generated;
import net.dv8tion.jda.api.entities.Guild;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class SettingsManager implements GuildSettingsManager<Server> {
   @ConfigProperty(name = "config.success")
   String successEmoji;
   @ConfigProperty(name = "config.warning")
   String warningEmoji;
   @ConfigProperty(name = "config.error")
   String errorEmoji;
   private final ServerRepository serverRepository;
   private final HashMap<Long, LanguageService> languageServices = new HashMap<>();

   @Inject
   public SettingsManager(ServerRepository serverRepository) {
      this.serverRepository = serverRepository;
   }

   public Server getSettings(Guild guild) {
      return guild == null ? null : this.getSettings(guild.getIdLong());
   }

   @Transactional
   public Server getSettings(long guildId) {
      return this.serverRepository.findByIdOptional(guildId).map(server -> {
         if (server.getManager() == null) {
            server.setManager(this);
         }

         return (Server)server;
      }).orElse(this.createDefaultSettings(guildId));
   }

   public LanguageService getLanguageService(Guild guild) {
      return guild == null ? null : this.getLanguageService(guild.getIdLong());
   }

   @Transactional
   public LanguageService getLanguageService(long guildId) {
      return this.languageServices
         .computeIfAbsent(
            guildId,
            id -> new LanguageService(
               // this.messageSource,
               this.getSettings(id).getLanguage() == null ? "en" : this.getSettings(id).getLanguage(),
               this.successEmoji,
               this.errorEmoji,
               this.warningEmoji
            )
         );
   }

   public void setLanguage(String lang, Guild guild) {
      if (guild != null) {
         this.setLanguage(lang, guild.getIdLong());
      }
   }

   @Transactional
   public void setLanguage(String lang, long guildId) {
      Server settings = this.getSettings(guildId);
      settings.setLanguage(lang);
      this.saveSettings(settings);
      this.languageServices.put(guildId, new LanguageService(/*this.messageSource,*/ lang, this.successEmoji, this.errorEmoji, this.warningEmoji));
   }

   @Transactional
   public void saveSettings(Server server) {
      this.serverRepository.getEntityManager().merge(server);
   }

   @Transactional
   public void addRoleManagerToServer(long guildId, com.eme22.bolo.model.RoleManager manager) {
      Server server = this.getSettings(guildId);
      server.addToRoleManagers(manager);
      this.serverRepository.getEntityManager().merge(server);
   }

   protected Server createDefaultSettings(long guildId) {
      return Server.builder()
         .manager(this)
         .id(guildId)
         .textChannelId(0L)
         .voiceChannelId(0L)
         .djRoleId(0L)
         .adminRoleId(0L)
         .volume(100)
         .defaultPlaylist(null)
         .repeatMode(RepeatMode.OFF)
         .prefix(null)
         .skipRatio(0.55)
         .bienvenidasChannelEnabled(false)
         .bienvenidasChannelId(0L)
         .bienvenidasChannelMessage(null)
         .bienvenidasChannelImage(null)
         .despedidasChannelImage(null)
         .despedidasChannelMessage(null)
         .despedidasChannelEnabled(false)
         .despedidasChannelId(0L)
         .birthdayChannelId(0L)
         .birthdays(new ArrayList<>())
         .memeImages(new ArrayList<>())
         .imageOnlyChannelsIds(new ArrayList<>())
         .roleManagerList(new ArrayList<>())
         .eightBallAnswers(new ArrayList<>())
         .antiRaidMode(false)

         .linkEnhancerEnabled(false)
         .backupEnabled(false)
         .language("en")
         .build();
   }

   @Transactional
   protected void deleteSettings(Guild guild) {
      this.serverRepository.deleteById(guild.getIdLong());
   }

   public void updateOldSettings(File oldSettings) {
   }

   @Generated
   public HashMap<Long, LanguageService> getLanguageServices() {
      return this.languageServices;
   }
}
