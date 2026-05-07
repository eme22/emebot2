package com.eme22.bolo.audio;

import com.eme22.bolo.Bot;
import dev.arbjerg.lavalink.client.LavalinkClient;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

@ApplicationScoped
public class AloneInVoiceHandler {
   private Bot bot;
   private LavalinkClient client;
   private final HashMap<Long, Instant> aloneSince = new HashMap<>();
   
   @ConfigProperty(name = "config.alonetimeuntilstop")
   long aloneTimeUntilStop;

   public void init(Bot bot) {
      this.bot = bot;
      if (this.aloneTimeUntilStop > 0L) {
         bot.getThreadpool().scheduleWithFixedDelay(this::check, 0L, 5L, TimeUnit.SECONDS);
      }
   }

   public void check() {
      Set<Long> toRemove = new HashSet<>();

      for (Entry<Long, Instant> entrySet : this.aloneSince.entrySet()) {
         if (entrySet.getValue().getEpochSecond() <= Instant.now().getEpochSecond() - this.aloneTimeUntilStop) {
            Guild guild = this.bot.getJDA().getGuildById(entrySet.getKey());
            if (guild == null) {
               toRemove.add(entrySet.getKey());
            } else {
               this.bot.getPlayerManager().getAudioHandler(guild).stopAndClear();
               guild.getJDA().getDirectAudioController().disconnect(guild);
               toRemove.add(entrySet.getKey());
            }
         }
      }

      toRemove.forEach(this.aloneSince::remove);
   }

   public void onVoiceUpdate(GuildVoiceUpdateEvent event) {
      if (this.aloneTimeUntilStop > 0L) {
         Guild guild = event.getEntity().getGuild();
         if (this.bot.getPlayerManager().getAudioHandler(guild) != null) {
            boolean alone = this.isAlone(guild);
            boolean inList = this.aloneSince.containsKey(guild.getIdLong());
            if (!alone && inList) {
               this.aloneSince.remove(guild.getIdLong());
            } else if (alone && !inList) {
               this.aloneSince.put(guild.getIdLong(), Instant.now());
            }
         }
      }
   }

   private boolean isAlone(Guild guild) {
      return guild.getSelfMember().getVoiceState().getChannel() == null
         ? false
         : guild.getSelfMember().getVoiceState().getChannel().getMembers().stream().noneMatch(x -> !x.getVoiceState().isDeafened() && !x.getUser().isBot());
   }
}
