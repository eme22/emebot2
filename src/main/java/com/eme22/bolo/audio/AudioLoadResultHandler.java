package com.eme22.bolo.audio;

import dev.arbjerg.lavalink.client.FunctionalLoadResultHandler;
import dev.arbjerg.lavalink.client.player.LoadFailed;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import dev.arbjerg.lavalink.client.player.Track;

public interface AudioLoadResultHandler {
   void trackLoaded(Track track);

   void playlistLoaded(PlaylistLoaded playlist);

   void noMatches();

   void loadFailed(LoadFailed throwable);

   void loadFailed(String error);

   FunctionalLoadResultHandler getRealResultHandler();
}
