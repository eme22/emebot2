package com.eme22.bolo.audio;

import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.queue.FairQueue;
import lombok.Generated;
@Slf4j
public class QueueManager {
   
   private final AudioHandler audioHandler;
   private final FairQueue<QueuedTrack> queue = new FairQueue<>();

   public QueueManager(AudioHandler audioHandler) {
      this.audioHandler = audioHandler;
   }

   public int addToTrackQueue(QueuedTrack clone) {
      if (this.audioHandler.getAudioPlayer().isPresent() && this.audioHandler.getAudioPlayer().get().getTrack() == null) {
         this.audioHandler.getAudioPlayer().get().setTrack(clone.getTrack()).subscribe();
         return -1;
      } else {
         return this.queue.add(clone);
      }
   }

   public void addAllToTrackQueue(QueuedTrack... clones) {
      for (QueuedTrack clone : clones) {
         this.queue.add(clone);
      }
   }

   public void addToTrackQueueAt(int position, QueuedTrack clone) {
      if (this.audioHandler.getAudioPlayer().isPresent() && this.audioHandler.getAudioPlayer().get().getTrack() == null) {
         this.audioHandler.getAudioPlayer().get().setTrack(clone.getTrack()).subscribe();
      } else {
         this.queue.addAt(position, clone);
      }
   }

   public void addTrackToFront(QueuedTrack queuedTrack) {
      if (this.audioHandler.getAudioPlayer().isPresent() && this.audioHandler.getAudioPlayer().get().getTrack() == null) {
         this.audioHandler.getAudioPlayer().get().setTrack(queuedTrack.getTrack()).subscribe();
      } else {
         this.queue.addAt(0, queuedTrack);
      }
   }

   public void shuffle() {
      this.queue.shuffle();
   }

   public void clear() {
      this.queue.clear();
   }

   @Generated
   public FairQueue<QueuedTrack> getQueue() {
      return this.queue;
   }
}
