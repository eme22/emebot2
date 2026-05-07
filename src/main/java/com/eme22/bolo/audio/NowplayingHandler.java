package com.eme22.bolo.audio;

import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.Bot;
import com.eme22.bolo.entities.Pair;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.model.Server;
import dev.arbjerg.lavalink.client.player.Track;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
@ApplicationScoped
@Slf4j
public class NowplayingHandler {
   
   private Bot bot;
   private final HashMap<Long, Pair<Long, Long>> lastNP = new HashMap<>();
   
   @ConfigProperty(name = "config.nowplayingimages")
   boolean npImages;
   
   @ConfigProperty(name = "config.songinstatus")
   boolean songInStatus;

   public void init(Bot bot) {
      this.bot = bot;
      if (!this.npImages) {
         bot.getThreadpool().scheduleWithFixedDelay(this::updateAll, 0L, 5L, TimeUnit.SECONDS);
      }
   }

   public void setLastNPMessage(Message m) {
      this.clearLastNPMessage(m.getGuild());
      this.lastNP.put(m.getGuild().getIdLong(), new Pair<>(m.getChannel().getIdLong(), m.getIdLong()));
   }

   public void clearLastNPMessage(Guild guild) {
      this.deleteLastMessage(this.lastNP.get(guild.getIdLong()));
      this.lastNP.remove(guild.getIdLong());
   }

   public void clearLastNPMessage(long guild) {
      this.deleteLastMessage(this.lastNP.get(guild));
      this.lastNP.remove(guild);
   }

   public void clearLastNPMessageSync(long guild) {
      this.deleteLastMessageSync(this.lastNP.get(guild));
      this.lastNP.remove(guild);
   }

   private void deleteLastMessageSync(Pair<Long, Long> last) {
      if (last != null) {
         try {
            this.bot.getJDA().getTextChannelById(last.getKey()).deleteMessageById(last.getValue()).complete();
         } catch (Exception ignored) {
         }
      }
   }

   public void disableLastNPMessage(long guildId) {
      Pair<Long, Long> pair = this.lastNP.get(guildId);
      if (pair != null) {
         TextChannel tc = this.bot.getJDA().getTextChannelById(pair.getKey());
         if (tc != null) {
            AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(guildId);
            tc.retrieveMessageById(pair.getValue()).queue(msg -> {
               tc.editMessageById(msg.getId(), handler.disableButtons(msg)).queue();
            }, t -> {});
         }
      }
      this.lastNP.remove(guildId);
   }

   private void deleteLastMessage(Pair<Long, Long> lastmessage) {
      if (lastmessage != null) {
         TextChannel music = this.bot.getJDA().getTextChannelById(lastmessage.getKey());
         if (music != null) {
            music.deleteMessageById(lastmessage.getValue()).queue();
         }
      }
   }

   private void updateAll() {
      try {
         Set<Long> toRemove = new HashSet<>();

         for (long guildId : this.lastNP.keySet()) {
            Guild guild = this.bot.getJDA().getGuildById(guildId);
            if (guild == null) {
               toRemove.add(guildId);
            } else {
               Pair<Long, Long> pair = this.lastNP.get(guildId);
               TextChannel tc = guild.getTextChannelById(pair.getKey());
                if (tc == null) {
                   toRemove.add(guildId);
                } else {
                   AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(guildId);
                   if (!handler.isMusicPlaying(this.bot.getJDA())) {
                      toRemove.add(guildId);
                      continue;
                   }

                   if (handler.getAudioPlayer().map(p -> p.getPaused()).orElse(false)) {
                      continue;
                   }

                   MessageCreateData msg = handler.getNowPlaying(this.bot.getJDA());
                   MessageEditBuilder builder = new MessageEditBuilder();
                   builder.setContent(msg.getContent());
                   builder.setEmbeds(msg.getEmbeds());
                   builder.setComponents(msg.getComponents());

                   try {
                      tc.editMessageById(pair.getValue(), builder.build()).queue(m -> {}, t -> this.lastNP.remove(guildId));
                   } catch (Exception var12) {
                      log.error("Error al actualizar el mensaje de nowplaying", var12);
                      toRemove.add(guildId);
                   }
                }
            }
         }

         toRemove.forEach(this.lastNP::remove);
      } catch (Exception var13) {
         log.error("Error al actualizar todos los mensajes de nowplaying", var13);
      }
   }

   public void updateTopic(long guildId, AudioHandler handler, boolean wait, LanguageService languageService) {
      Guild guild = this.bot.getJDA().getGuildById(guildId);
      if (guild != null) {
         Server settings = this.bot.getSettingsManager().getSettings(guildId);
         TextChannel tchan = guild.getTextChannelById(settings.getTextChannelId());
         if (tchan != null && guild.getSelfMember().hasPermission(tchan, new Permission[]{Permission.MANAGE_CHANNEL})) {
            String topic = tchan.getTopic();
            String otherText;
            if (topic == null || topic.isEmpty()) {
               otherText = "\u200b";
            } else if (topic.contains("\u200b")) {
               otherText = topic.substring(topic.lastIndexOf("\u200b"));
            } else {
               otherText = "\u200b\n " + topic;
            }

            String text = handler.getTopicFormat(this.bot.getJDA(), languageService) + otherText;
            if (!text.equals(tchan.getTopic())) {
               try {
                  ((TextChannelManager)tchan.getManager().setTopic(text)).complete(wait);
               } catch (PermissionException var13) {
                  log.warn("No tengo permiso para cambiar el topic en {} - {}", guild.getName(), tchan.getName());
               } catch (RateLimitedException var14) {
                  log.warn("La accion se ha ratelimitado, no se volvera a intentar hasta el siguiente evento");
               }
            }
         }
      }
   }

   public void onTrackUpdate(long guildId, Track track, AudioHandler handler, LanguageService languageService) {
      if (this.songInStatus) {
         if (track != null && this.bot.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inAudioChannel()).count() <= 1L) {
            this.bot.getJDA().getPresence().setActivity(Activity.listening(track.getInfo().getTitle()));
         } else {
            this.bot.resetGame();
         }
      }

      this.updateTopic(guildId, handler, false, languageService);
   }

   public Pair<Long, Long> getLastNP(Guild guild) {
      return this.lastNP.get(guild.getIdLong());
   }

   public void onMessageDelete(Guild guild, long messageId) {
      Pair<Long, Long> pair = this.lastNP.get(guild.getIdLong());
      if (pair != null) {
         if (pair.getValue() == messageId) {
            this.lastNP.remove(guild.getIdLong());
         }
      }
   }
}
