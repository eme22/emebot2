package com.eme22.bolo.audio;

import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.queue.Queueable;
import com.eme22.bolo.utils.FormatUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.Generated;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

@Slf4j
public class QueuedTrack implements Queueable {
   @Generated
   
   private final Track track;
   private static final ObjectMapper mapper = new ObjectMapper();

   public QueuedTrack(Track track, User owner, long guild) {
      this(track, new RequestMetadata(owner, guild));
   }

   public QueuedTrack(Track track, User owner, Guild guild) {
      this(track, new RequestMetadata(owner, guild));
   }

   public QueuedTrack(Track track, RequestMetadata rm) {
      this.track = track;
      track.setUserData(rm);
   }

   @Override
   public String getIdentifier() {
      return String.valueOf(this.track.getUserData(RequestMetadata.class).owner());
   }

   @Override
   public String getUserData() {
      return this.track.getUserData().asText();
   }

   @Override
   public String toString() {
      try {
         return "`["
            + FormatUtil.formatTime(this.track.getInfo().getLength())
            + "]` [**"
            + this.track.getInfo().getTitle()
            + "**]("
            + this.track.getInfo().getUri()
            + ") - <@"
            + ((RequestMetadata)mapper.readValue(this.track.getUserData().toString(), RequestMetadata.class)).owner()
            + ">";
      } catch (JsonProcessingException var2) {
         return "`["
            + FormatUtil.formatTime(this.track.getInfo().getLength())
            + "]` [**"
            + this.track.getInfo().getTitle()
            + "**]("
            + this.track.getInfo().getUri()
            + ")";
      }
   }

   @Generated
   public Track getTrack() {
      return this.track;
   }
}

