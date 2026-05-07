package com.eme22.bolo.backup;

import com.eme22.bolo.Bot;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.model.backup.Adjunto;
import com.eme22.bolo.model.backup.BackupData;
import com.eme22.bolo.model.backup.Canal;
import com.eme22.bolo.model.backup.Mensaje;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.Nullable;

public class BackupManager implements Runnable {
   Bot bot;
   private Gson gson = new Gson();

   public BackupManager(Bot bot) {
      this.bot = bot;
   }

   void backup(long guild) {
      Guild g = this.bot.getJDA().getGuildById(guild);
      if (g != null) {
         Server settings = this.bot.getSettingsManager().getSettings(guild);
         if (settings != null) {
            if (settings.isBackupEnabled()) {
               List<Canal> canales = new ArrayList<>();
               g.getChannels(true).forEach(c -> {
                  Canal canal = null;
                  if (c.getType().isMessage()) {
                     MessageHistory history = ((MessageChannel)c).getHistory();
                     List<Mensaje> dataxx = extractMessages(history);
                     canal = new Canal(c.getName(), c.getType().name(), dataxx);
                  } else if (c.getType().isAudio()) {
                     canal = new Canal(c.getName(), c.getType().name(), new ArrayList<>());
                  } else if (c.getType().isGuild()) {
                     canal = new Canal(c.getName(), c.getType().name(), new ArrayList<>());
                  } else if (c.getType().isThread()) {
                     MessageHistory history = ((ThreadChannel)c).getHistory();
                     List<Mensaje> datax = extractMessages(history);
                     canal = new Canal(c.getName(), c.getType().name(), datax);
                  }

                  if (canal != null) {
                     canales.add(canal);
                  }
               });
               BackupData data = new BackupData(canales, g.getName(), new ArrayList<>());

               try {
                  this.saveBackup(data);
               } catch (IOException var9) {
                  throw new RuntimeException(var9);
               }
            }
         }
      }
   }

   private void saveBackup(BackupData data) throws IOException {
      File folder = Files.createTempDirectory("server").toFile();
      if (!folder.exists()) {
         folder.mkdir();
      }

      File server = new File(folder, data.nombre());
      server.mkdir();
      File backup = new File(server, "backup.json");
      Files.writeString(backup.toPath(), this.gson.toJson(data));
      data.canal().forEach(canal -> {
         canal.mensajes().forEach(mensaje -> {
            if (mensaje.adjuntos() != null) {
               mensaje.adjuntos().forEach(a -> this.downloadAttachment(server, a));
            }
         });
      });
      File zip = new File(folder, "backup-" + System.currentTimeMillis() + ".zip");
      zip(server.getPath(), zip.getPath());
   }

   private void downloadAttachment(File server, Adjunto a) {
      try {
         URL url = java.net.URI.create(a.url()).toURL();
         Path path = Paths.get(server.getPath(), a.nombre());
         Files.copy(url.openStream(), path);
      } catch (IOException var5) {
         throw new RuntimeException(var5);
      }
   }

   @Nullable
   private static List<Mensaje> extractMessages(MessageHistory history) {
      List<Mensaje> mensajes = null;
      List<Adjunto> attachments = new ArrayList<>();

      List<Message> messages;
      while (!(messages = history.retrievePast(100).complete()).isEmpty()) {
         try {
            Thread.sleep(1000L);
         } catch (InterruptedException var5) {
            throw new RuntimeException(var5);
         }

         mensajes = messages.stream()
            .map(
               m -> {
                  if (m.getAttachments().isEmpty()) {
                     return new Mensaje(m.getAuthor().getId(), m.getContentRaw(), m.getTimeCreated().toInstant().toEpochMilli(), null);
                  } else {
                     attachments.add(new Adjunto(m.getAttachments().get(0).getUrl(), m.getAttachments().get(0).getFileName()));
                     return new Mensaje(
                        m.getAuthor().getId(),
                        m.getContentRaw(),
                        m.getTimeCreated().toInstant().toEpochMilli(),
                        m.getAttachments().stream().map(attachment ->
                                new Adjunto(attachment.getUrl(), attachment.getFileName())
                        ).toList()
                     );
                  }
               }
            )
            .toList();
      }

      return mensajes;
   }

   @Override
   public void run() {
      this.bot.getJDA().getGuilds().forEach(g -> this.backup(g.getIdLong()));
   }

   public static void zip(String sourceDirPath, String zipFilePath) throws IOException {
      Path p = Files.createFile(Paths.get(zipFilePath));

      try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
         Path pp = Paths.get(sourceDirPath);
         Files.walk(pp).filter(path -> !Files.isDirectory(path)).forEach(path -> {
            ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());

            try {
               zs.putNextEntry(zipEntry);
               Files.copy(path, zs);
               zs.closeEntry();
            } catch (IOException var5) {
               System.err.println(var5);
            }
         });
      }
   }
}
