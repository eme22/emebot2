package com.eme22.bolo.playlist;

import com.eme22.bolo.audio.AudioLoadResultHandler;
import com.eme22.bolo.utils.FormatUtil;
import com.eme22.bolo.utils.OtherUtil;
import dev.arbjerg.lavalink.client.FunctionalLoadResultHandler;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LoadFailed;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.client.player.TrackException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Generated;
import lombok.extern.log4j.Log4j2;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Log4j2
@ApplicationScoped
public class PlaylistLoader {
   @ConfigProperty(name = "config.playlistsfolder")
   String playlistsFolder;
   @ConfigProperty(name = "config.maxseconds")
   long maxSeconds;

   public List<String> getPlaylistNames() {
      if (this.playlistsFolder == null) {
         log.error("Playlists folder is not configured!");
         return Collections.emptyList();
      }
      if (this.folderExists()) {
         Path path = OtherUtil.getPath(this.playlistsFolder);
         if (path == null) return Collections.emptyList();
         File folder = new File(path.toString());
         File[] files = folder.listFiles(pathname -> pathname.getName().endsWith(".txt"));
         if (files == null) return Collections.emptyList();
         return Arrays.stream(files)
            .map(f -> f.getName().substring(0, f.getName().length() - 4))
            .collect(Collectors.toList());
      } else {
         this.createFolder();
         return Collections.emptyList();
      }
   }

   public void createFolder() {
      if (this.playlistsFolder == null) return;
      try {
         Path path = OtherUtil.getPath(this.playlistsFolder);
         if (path != null) Files.createDirectory(path);
      } catch (IOException var2) {
      }
   }

   public boolean folderExists() {
      if (this.playlistsFolder == null) return false;
      Path path = OtherUtil.getPath(this.playlistsFolder);
      return path != null && Files.exists(path);
   }

   public void createPlaylist(String name) throws IOException {
      if (this.playlistsFolder == null) throw new IOException("Playlists folder is not configured");
      Path path = OtherUtil.getPath(this.playlistsFolder + File.separator + name + ".txt");
      if (path == null) throw new IOException("Invalid path");
      Files.createFile(path);
   }

   public void deletePlaylist(String name) throws IOException {
      if (this.playlistsFolder == null) throw new IOException("Playlists folder is not configured");
      Path path = OtherUtil.getPath(this.playlistsFolder + File.separator + name + ".txt");
      if (path == null) throw new IOException("Invalid path");
      Files.delete(path);
   }

   public void writePlaylist(String name, String text) throws IOException {
      if (this.playlistsFolder == null) throw new IOException("Playlists folder is not configured");
      Path path = OtherUtil.getPath(this.playlistsFolder + File.separator + name + ".txt");
      if (path == null) throw new IOException("Invalid path");
      Files.write(path, text.trim().getBytes());
   }

   public PlaylistLoader.Playlist getPlaylist(String name) {
      if (!this.getPlaylistNames().contains(name)) {
         return null;
      } else {
         try {
            if (this.folderExists()) {
               boolean[] shuffle = new boolean[]{false};
               List<String> list = new ArrayList<>();
               Path path = OtherUtil.getPath(this.playlistsFolder + File.separator + name + ".txt");
               if (path == null) return null;
               Files.readAllLines(path).forEach(str -> {
                  String s = str.trim();
                  if (!s.isEmpty()) {
                     if (!s.startsWith("#") && !s.startsWith("//")) {
                        list.add(s);
                     } else {
                        s = s.replaceAll("\\s+", "");
                        if (s.equalsIgnoreCase("#shuffle") || s.equalsIgnoreCase("//shuffle")) {
                           shuffle[0] = true;
                        }
                     }
                  }
               });
               if (shuffle[0]) {
                  shuffle(list);
               }

               return new PlaylistLoader.Playlist(name, list, shuffle[0]);
            } else {
               this.createFolder();
               return null;
            }
         } catch (IOException var4) {
            return null;
         }
      }
   }

   private static <T> void shuffle(List<T> list) {
      for (int first = 0; first < list.size(); first++) {
         int second = (int)(Math.random() * list.size());
         T tmp = list.get(first);
         list.set(first, list.get(second));
         list.set(second, tmp);
      }
   }

   public class Playlist {
      private final String name;
      private final List<String> items;
      private final boolean shuffle;
      private final List<Track> tracks = new LinkedList<>();
      private final List<PlaylistLoader.PlaylistLoadError> errors = new LinkedList<>();
      private boolean loaded = false;

      private Playlist(String name, List<String> items, boolean shuffle) {
         this.name = name;
         this.items = items;
         this.shuffle = shuffle;
      }

      public void loadTracks(Link link, Consumer<Track> consumer, Runnable callback) {
         if (!this.loaded) {
            this.loaded = true;

            for (int i = 0; i < this.items.size(); i++) {
               final boolean last = i + 1 == this.items.size();
               final int index = i;
               AudioLoadResultHandler loadResultHandler = new AudioLoadResultHandler() {
                  private final FunctionalLoadResultHandler resultHandler = new FunctionalLoadResultHandler(
                     trackLoaded -> this.trackLoaded(trackLoaded.getTrack()), this::playlistLoaded, searchResult -> {}, this::noMatches, this::loadFailed
                  );

                  private void done() {
                     if (last) {
                        if (Playlist.this.shuffle) {
                           Playlist.this.shuffleTracks();
                        }

                        if (callback != null) {
                           callback.run();
                        }
                     }
                  }

                  @Override
                  public void trackLoaded(Track at) {
                     if (this.isTooLong(at)) {
                        Playlist.this.errors
                           .add(new PlaylistLoader.PlaylistLoadError(index, Playlist.this.items.get(index), "This track is longer than the allowed maximum"));
                     } else {
                        at.setUserData(0L);
                        Playlist.this.tracks.add(at);
                        consumer.accept(at);
                     }

                     this.done();
                  }

                  @Override
                  public void playlistLoaded(PlaylistLoaded ap) {
                     if (ap.getInfo().getSelectedTrack() > -1) {
                        this.trackLoaded((Track)ap.getTracks().get(ap.getInfo().getSelectedTrack()));
                     } else {
                        List<Track> loaded = new ArrayList<>(ap.getTracks());
                        if (Playlist.this.shuffle) {
                           for (int first = 0; first < loaded.size(); first++) {
                              int second = (int)(Math.random() * loaded.size());
                              Track tmp = loaded.get(first);
                              loaded.set(first, loaded.get(second));
                              loaded.set(second, tmp);
                           }
                        }

                        loaded.removeIf(this::isTooLong);
                        loaded.forEach(at -> at.setUserData(0L));
                        Playlist.this.tracks.addAll(loaded);
                        loaded.forEach(at -> consumer.accept(at));
                     }

                     this.done();
                  }

                  public String getMaxTime() {
                     return FormatUtil.formatTime(PlaylistLoader.this.maxSeconds * 1000L);
                  }

                  public boolean isTooLong(Track track) {
                     return PlaylistLoader.this.maxSeconds <= 0L ? false : Math.round(track.getInfo().getLength() / 1000.0) > PlaylistLoader.this.maxSeconds;
                  }

                  @Override
                  public void noMatches() {
                     Playlist.this.errors.add(new PlaylistLoader.PlaylistLoadError(index, Playlist.this.items.get(index), "No matches found."));
                     this.done();
                  }

                  @Override
                  public void loadFailed(LoadFailed lf) {
                     TrackException fe = lf.getException();
                     Playlist.this.errors
                        .add(new PlaylistLoader.PlaylistLoadError(index, Playlist.this.items.get(index), "Failed to load track: " + fe.getMessage()));
                     this.done();
                  }

                  @Override
                  public void loadFailed(String error) {
                     Playlist.this.errors.add(new PlaylistLoader.PlaylistLoadError(index, Playlist.this.items.get(index), error));
                     this.done();
                  }

                  @Override
                  public FunctionalLoadResultHandler getRealResultHandler() {
                     return this.resultHandler;
                  }
               };
               link.loadItem(this.items.get(i)).subscribe(loadResultHandler.getRealResultHandler());
            }
         }
      }

      public void shuffleTracks() {
         PlaylistLoader.shuffle(this.tracks);
      }

      @Generated
      public String getName() {
         return this.name;
      }

      @Generated
      public List<String> getItems() {
         return this.items;
      }

      @Generated
      public boolean isShuffle() {
         return this.shuffle;
      }

      @Generated
      public List<Track> getTracks() {
         return this.tracks;
      }

      @Generated
      public List<PlaylistLoader.PlaylistLoadError> getErrors() {
         return this.errors;
      }

      @Generated
      public boolean isLoaded() {
         return this.loaded;
      }

      @Generated
      public void setLoaded(final boolean loaded) {
         this.loaded = loaded;
      }

      @Generated
      @Override
      public boolean equals(final Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof PlaylistLoader.Playlist other)) {
            return false;
         } else if (!other.canEqual(this)) {
            return false;
         } else if (this.isShuffle() != other.isShuffle()) {
            return false;
         } else if (this.isLoaded() != other.isLoaded()) {
            return false;
         } else {
            Object this$name = this.getName();
            Object other$name = other.getName();
            if (this$name == null ? other$name == null : this$name.equals(other$name)) {
               Object this$items = this.getItems();
               Object other$items = other.getItems();
               if (this$items == null ? other$items == null : this$items.equals(other$items)) {
                  Object this$tracks = this.getTracks();
                  Object other$tracks = other.getTracks();
                  if (this$tracks == null ? other$tracks == null : this$tracks.equals(other$tracks)) {
                     Object this$errors = this.getErrors();
                     Object other$errors = other.getErrors();
                     return this$errors == null ? other$errors == null : this$errors.equals(other$errors);
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      }

      @Generated
      protected boolean canEqual(final Object other) {
         return other instanceof PlaylistLoader.Playlist;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         result = result * 59 + (this.isShuffle() ? 79 : 97);
         result = result * 59 + (this.isLoaded() ? 79 : 97);
         Object $name = this.getName();
         result = result * 59 + ($name == null ? 43 : $name.hashCode());
         Object $items = this.getItems();
         result = result * 59 + ($items == null ? 43 : $items.hashCode());
         Object $tracks = this.getTracks();
         result = result * 59 + ($tracks == null ? 43 : $tracks.hashCode());
         Object $errors = this.getErrors();
         return result * 59 + ($errors == null ? 43 : $errors.hashCode());
      }

      @Generated
      @Override
      public String toString() {
         return "PlaylistLoader.Playlist(name="
            + this.getName()
            + ", items="
            + this.getItems()
            + ", shuffle="
            + this.isShuffle()
            + ", tracks="
            + this.getTracks()
            + ", errors="
            + this.getErrors()
            + ", loaded="
            + this.isLoaded()
            + ")";
      }
   }

   public record PlaylistLoadError(int index, String item, String reason) {
   }
}
