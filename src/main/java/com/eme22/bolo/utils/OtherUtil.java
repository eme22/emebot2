package com.eme22.bolo.utils;

import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.Bot;
import com.eme22.bolo.EMBotApplication;
import com.eme22.bolo.entities.Pair;
import com.eme22.bolo.entities.Prompt;
import com.eme22.bolo.language.LanguageService;

import com.eme22.bolo.model.Server;
import com.eme22.bolo.stats.StatsService;
import com.eme22.discordcdn.Discord;
import com.eme22.discordcdn.model.RefreshedUrl;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jlyrics.Lyrics;
import com.jagrosh.jlyrics.LyricsClient;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D.Float;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import lombok.Generated;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.OkHttpClient.Builder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.eclipse.microprofile.config.ConfigProvider;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OtherUtil {
   
   public static final String NEW_VERSION_AVAILABLE = "Hay una nueva version del bot!\nActual: %s\nNueva: %s\n\nVisite https://github.com/eme22/PGMUSICBOT/releases/latest para obtener la ultima version.";
   private static final String WINDOWS_INVALID_PATH = "c:\\windows\\system32\\";
   
   public static boolean checkDJPermission(Bot bot, Member member) {
      if (member.hasPermission(net.dv8tion.jda.api.Permission.MANAGE_SERVER)) return true;
      Server settings = bot.getSettingsManager().getSettings(member.getGuild().getIdLong());
      if (settings != null && settings.getDjRoleId() != 0) {
          return member.getRoles().stream().anyMatch(r -> r.getIdLong() == settings.getDjRoleId());
      }
      return true;
   }

   private static final List<String> SHA = new ArrayList<>();
   private static final int WIDTH = 1000;
   private static final int HEIGHT = 500;
   private static final int PADDING = 50;
   private static final int AVATAR_SIZE = 250;
   
   private static String welcomeString = ConfigProvider.getConfig().getValue("config.welcome", String.class);
   private static String goodByeString = ConfigProvider.getConfig().getValue("config.goodbye", String.class);

   public static Path getPath(String path) {
      Path result = Paths.get(path);
      if (result.toAbsolutePath().toString().toLowerCase().startsWith("c:\\windows\\system32\\")) {
         try {
            result = Paths.get(
               new File(EMBotApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath() + File.separator + path
            );
         } catch (URISyntaxException var3) {
         }
      }

      return result;
   }

   public static String loadResource(Object clazz, String name) {
      try {
         String var4;
         try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(clazz.getClass().getResourceAsStream(name))))) {
            StringBuilder sb = new StringBuilder();
            reader.lines().forEach(line -> sb.append("\r\n").append(line));
            var4 = sb.toString().trim();
         }

         return var4;
      } catch (IOException var7) {
         return null;
      }
   }

   public static InputStream imageFromUrl(String url, String token) {
      if (url == null) {
         return null;
      } else {
         try {
            URL u = java.net.URI.create(url).toURL();
            HttpURLConnection urlConnection = (HttpURLConnection)u.openConnection();
            urlConnection.setRequestProperty(
               "user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.112 Safari/537.36"
            );
            int responseCode = urlConnection.getResponseCode();
            return responseCode == 404 && url.contains("cdn.discordapp.com")
               ? imageFromUrl(((RefreshedUrl)new Discord(token).fetchLatestLink(url).getRefreshedUrls().get(0)).getRefreshed(), token)
               : urlConnection.getInputStream();
         } catch (IllegalArgumentException | IOException var5) {
            return null;
         }
      }
   }

   public static Activity parseGame(String game) {
      return getActivity(game);
   }

   public static String makeNonEmpty(String str) {
      return str != null && !str.isEmpty() ? str : "\u200b";
   }

   public static OnlineStatus parseStatus(String status) {
      if (status != null && !status.trim().isEmpty()) {
         OnlineStatus st = OnlineStatus.fromKey(status);
         return st == null ? OnlineStatus.ONLINE : st;
      } else {
         return OnlineStatus.ONLINE;
      }
   }

   public static String checkVersion(Prompt prompt) {
      String version = getCurrentVersion();
      String latestVersion = getLatestVersion();
      if (latestVersion != null && !latestVersion.equals(version)) {
         prompt.alert(
            Prompt.Level.WARNING,
            "Version",
            String.format(
               "Hay una nueva version del bot!\nActual: %s\nNueva: %s\n\nVisite https://github.com/eme22/PGMUSICBOT/releases/latest para obtener la ultima version.",
               version,
               latestVersion
            )
         );
      }

      return version;
   }

   public static String getCurrentVersion() {
      return EMBotApplication.class.getPackage() != null && EMBotApplication.class.getPackage().getImplementationVersion() != null
         ? EMBotApplication.class.getPackage().getImplementationVersion()
         : "UNKNOWN";
   }

   public static String getLatestVersion() {
      try {
         Response response = new Builder()
            .build()
            .newCall(new okhttp3.Request.Builder().get().url("https://api.github.com/repos/eme22/PGMUSICBOT/releases/latest").build())
            .execute();
         ResponseBody body = response.body();
         if (body != null) {
            String var4;
            try (Reader reader = body.charStream()) {
               JSONObject obj = new JSONObject(new JSONTokener(reader));
               var4 = obj.getString("tag_name");
            } finally {
               response.close();
            }

            return var4;
         } else {
            return null;
         }
      } catch (JSONException | NullPointerException | IOException var13) {
         return null;
      }
   }

   public static Activity getActivity(String game) {
      if (game != null && !game.trim().isEmpty() && !game.trim().equalsIgnoreCase("default")) {
         String lower = game.toLowerCase();
         if (lower.startsWith("playing")) {
            return Activity.playing(makeNonEmpty(game.substring(7).trim()));
         } else if (lower.startsWith("listening to")) {
            return Activity.listening(makeNonEmpty(game.substring(12).trim()));
         } else if (lower.startsWith("listening")) {
            return Activity.listening(makeNonEmpty(game.substring(9).trim()));
         } else if (lower.startsWith("watching")) {
            return Activity.watching(makeNonEmpty(game.substring(8).trim()));
         } else {
            if (lower.startsWith("streaming")) {
               String[] parts = game.substring(9).trim().split("\\s+", 2);
               if (parts.length == 2) {
                  return Activity.streaming(makeNonEmpty(parts[1]), "https://twitch.tv/" + parts[0]);
               }
            }

            return Activity.playing(game);
         }
      } else {
         return null;
      }
   }







   public static String getFancyProgressBar(long elapsedTime, long totalTime, boolean isPlaying) {
      int barWidth = 16;
      double progress = (double)elapsedTime / totalTime;
      int currentLength = (int)(progress * barWidth);
      String playButton = isPlaying ? "â¸" : "â–¶";
      StringBuilder sb = new StringBuilder(playButton);
      sb.append("\ud83d\udd18".repeat(Math.max(0, currentLength)));
      sb.append("Â·".repeat(Math.max(0, barWidth - currentLength)));
      long minutes = elapsedTime / 1000L / 60L;
      long seconds = elapsedTime / 1000L % 60L;
      String formattedTime = String.format("%d:%02d", minutes, seconds);
      sb.append(" ").append(formattedTime).append("/");
      minutes = totalTime / 1000L / 60L;
      seconds = totalTime / 1000L % 60L;
      formattedTime = String.format("%d:%02d", minutes, seconds);
      sb.append(formattedTime);
      return sb.toString();
   }

   public static void crateImage2(String username, String message, BufferedImage background, BufferedImage avatar, String outputFilePath) throws IOException, FontFormatException {
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("trans.ttf");
      if (is == null) {
         log.error("No hay una fuente ttf configurada!!!");
      } else {
         BufferedImage welcomeImage = new BufferedImage(1000, 500, 2);
         Graphics2D g2d = welcomeImage.createGraphics();
         g2d.drawImage(background, 0, 0, 1000, 500, null);
         Font font = Font.createFont(0, is).deriveFont(90.0F);
         g2d.setFont(font);
         g2d.setColor(Color.WHITE);
         FontMetrics fontMetrics = g2d.getFontMetrics();
         int usernameWidth = fontMetrics.stringWidth(username);
         int usernameHeight = fontMetrics.getHeight();
         int usernameX = (1000 - usernameWidth) / 2;
         int usernameY = 50 + usernameHeight;
         g2d.drawString(username, usernameX, usernameY);
         BufferedImage circleImage = createCircleImage(avatar, 250);
         int avatarX = 375;
         int avatarY = (200 - usernameHeight) / 2 + usernameHeight;
         g2d.drawImage(circleImage, avatarX, avatarY, null);
         font = font.deriveFont(70.0F);
         g2d.setFont(font);
         g2d.setColor(Color.WHITE);
         int welcomeWidth = fontMetrics.stringWidth(message);
         int welcomeHeight = fontMetrics.getHeight();
         int welcomeX = (1000 - welcomeWidth) / 2;
         int welcomeY = 450 - welcomeHeight;
         g2d.drawString(message, welcomeX, welcomeY);
         g2d.dispose();
         File outputfile = new File(outputFilePath);
         ImageIO.write(welcomeImage, "png", outputfile);
      }
   }

   private static BufferedImage createCircleImage(BufferedImage image, int size) {
      BufferedImage circleImage = new BufferedImage(size, size, 2);
      Graphics2D g2d = circleImage.createGraphics();
      g2d.setColor(Color.BLACK);
      g2d.fillOval(0, 0, size, size);
      int x = (size - image.getWidth()) / 2;
      int y = (size - image.getHeight()) / 2;
      g2d.drawImage(image, x, y, null);
      g2d.dispose();
      return circleImage;
   }

   public static void createImage(String message, String name, String id, InputStream background, String userImage, File image, String token) throws IOException {
      try {
         int width = 1000;
         int height = 500;
         BufferedImage userPic = null;

         try {
            InputStream userPicStream = imageFromUrl(userImage, token);
            userPic = ImageIO.read(userPicStream);
            userPicStream.close();
         } catch (IIOException var16) {
            log.error("Exception", var16);
         }

         if (userPic != null) {
            userPic = createAvatar(userPic);
         }

         BufferedImage background2 = ImageIO.read(background);
         BufferedImage bi = new BufferedImage(width, height, 2);
         Graphics2D ig2 = bi.createGraphics();
         ig2.drawImage(background2, 0, 0, width, height, null);
         if (userPic != null) {
            ig2.drawImage(userPic, 370, 25, null);
         }

         InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("trans.ttf");
         if (is == null) {
            log.error("No hay una fuente ttf configurada!!!");
            return;
         }

         Font font2 = Font.createFont(0, is).deriveFont(90.0F);
         Font font1 = font2.deriveFont(70.0F);
         ig2.setFont(font1);
         drawOutlinedAndCenteredString(message, width, height, ig2, 370);
         ig2.setFont(font2);
         ig2.setPaint(Color.white);
         drawOutlinedAndCenteredString(name, width, height, ig2, 470);
         ig2.dispose();
         ImageIO.write(bi, "png", image);
      } catch (FontFormatException var17) {
         var17.printStackTrace();
      }
   }

   private static void drawOutlinedAndCenteredString(String s, int w, int h, @NotNull Graphics2D g, int fh) {
      FontMetrics fm = g.getFontMetrics();
      int x = (w - fm.stringWidth(s)) / 2;
      int y = fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2;
      y = fh == 0 ? y : fh;
      g.setColor(Color.black);
      g.drawString(s, x + 10, y);
      g.drawString(s, x - 10, y);
      g.drawString(s, x, y + 10);
      g.drawString(s, x, y - 10);
      g.setColor(Color.white);
      g.drawString(s, x, fh == 0 ? y : fh);
   }

   private static void drawCenteredString(String s, int w, int h, Graphics2D g, int fw, int fh, Color color) {
      FontMetrics fm = g.getFontMetrics();
      int x = (w - fm.stringWidth(s)) / 2;
      int y = fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2;
      g.setColor(color);
      g.drawString(s, fw == 0 ? x : fw, fh == 0 ? y : fh);
   }

   private static BufferedImage createAvatar(BufferedImage image) {
      int w = image.getWidth();
      int h = image.getHeight();
      BufferedImage output = new BufferedImage(w + 10, h + 10, 2);
      Graphics2D g2 = output.createGraphics();
      g2.setComposite(AlphaComposite.Src);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(Color.WHITE);
      g2.fill(new Float(0.0F, 0.0F, w, h));
      g2.setComposite(AlphaComposite.SrcAtop);
      g2.drawImage(image, 0, 0, null);
      g2.setColor(Color.WHITE);
      g2.setStroke(new BasicStroke(10.0F));
      g2.drawOval(0, 0, w, h);
      g2.dispose();
      Image tmp = output.getScaledInstance(300, 300, 4);
      output = new BufferedImage(300, 300, 2);
      g2 = output.createGraphics();
      g2.drawImage(tmp, 0, 0, null);
      g2.dispose();
      return output;
   }

   private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
      FileInputStream fis = new FileInputStream(file);
      byte[] byteArray = new byte[1024];

      int bytesCount;
      while ((bytesCount = fis.read(byteArray)) != -1) {
         digest.update(byteArray, 0, bytesCount);
      }

      fis.close();
      byte[] bytes = digest.digest();
      StringBuilder sb = new StringBuilder();

      for (byte aByte : bytes) {
         sb.append(Integer.toString((aByte & 255) + 256, 16).substring(1));
      }

      return sb.toString();
   }

   public static boolean hasValue(JSONArray json, String value) {
      for (int i = 0; i < json.length(); i++) {
         if (json.get(i).equals(value)) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasValue(JSONArray json, Integer value) {
      for (int i = 0; i < json.length(); i++) {
         if (json.get(i).equals(value)) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasValue(JSONArray json, Long value) {
      for (int i = 0; i < json.length(); i++) {
         if (json.get(i).equals(value)) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasValue(JSONArray json, Map<String, String> value) {
      for (int i = 0; i < json.length(); i++) {
         if (json.get(i).equals(value)) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasValue(JSONArray json, Pair<String, String> value) {
      for (int i = 0; i < json.length(); i++) {
         if (json.get(i).equals(value)) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasValue(JSONArray json, JSONArray value) {
      for (int i = 0; i < json.length(); i++) {
         if (json.get(i).equals(value)) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasValue(ArrayList<Long> list, Long channel) {
      for (Long aLong : list) {
         if (aLong.equals(channel)) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasValue(ArrayList<Pair<String, String>> data, Pair<String, String> meme) {
      for (Pair<String, String> num : data) {
         if (num.equals(meme)) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasValue(HashSet<Long> list, Long channel) {
      for (Long aLong : list) {
         if (aLong.equals(channel)) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasValue(List<Map<String, String>> list, Map<String, String> meme) {
      for (Map<String, String> data : list) {
         if (data.equals(meme)) {
            return true;
         }
      }

      return false;
   }


   public static int compare(String v1, String v2) {
      String s1 = normalisedVersion(v1);
      String s2 = normalisedVersion(v2);
      return s1.compareTo(s2);
   }

   public static String normalisedVersion(String version) {
      return normalisedVersion(version, ".", 4);
   }

   public static String normalisedVersion(String version, String sep, int maxWidth) {
      String[] split = Pattern.compile(sep, 16).split(version);
      StringBuilder sb = new StringBuilder();

      for (String s : split) {
         sb.append(String.format("%" + maxWidth + "s", s));
      }

      return sb.toString();
   }

   public static boolean isValidUrl(String imageAddress) {
      try {
         java.net.URI.create(imageAddress).toURL();
         return true;
      } catch (IllegalArgumentException | MalformedURLException var2) {
         return false;
      }
   }

   public static boolean checkImage(String imageAddress) {
      if (isValidUrl(imageAddress)) {
         HttpURLConnection connection;
         try {
            connection = (HttpURLConnection)java.net.URI.create(imageAddress).toURL().openConnection();
            connection.setRequestMethod("HEAD");
         } catch (IOException var3) {
            var3.printStackTrace();
            return false;
         }

         String contentType = connection.getHeaderField("Content-Type");
         return contentType.startsWith("image/");
      } else {
         return false;
      }
   }

   public static InputStream getBackground(Server settingsTEST, boolean b, String token) {
      if (b) {
         String image = settingsTEST.getBienvenidasChannelImage();
         if (image == null) {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            return classloader.getResourceAsStream("images/bienvenida.png");
         } else {
            return imageFromUrl(image, token);
         }
      } else {
         String image = settingsTEST.getDespedidasChannelImage();
         if (image == null) {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            return classloader.getResourceAsStream("images/despedida.png");
         } else {
            return imageFromUrl(image, token);
         }
      }
   }

   public static String getMessage(Bot bot, Guild guild, boolean b) {
      Server settings = bot.getSettingsManager().getSettings(guild.getIdLong());
      String message = b ? settings.getBienvenidasChannelMessage() : settings.getDespedidasChannelMessage();
      if (message == null) {
         return b ? welcomeString : goodByeString;
      } else {
         return message;
      }
   }

   public static boolean isAudioChannelAllowed(Guild guild, Server settings, Member member) {
      VoiceChannel current = guild.getSelfMember().getVoiceState().getChannel().asVoiceChannel();
      GuildVoiceState userState = member.getVoiceState();
      if (current == null) {
         current = guild.getVoiceChannelById(settings.getVoiceChannelId());
         return current == null ? true : userState.getChannel().equals(current);
      } else {
         return userState.getChannel().equals(current);
      }
   }

   public static int isUserInVoice(Guild guild, Server settings, Member member) {
      GuildVoiceState userState = member.getVoiceState();
      if (userState != null && userState.inAudioChannel()) {
         VoiceChannel afkChannel = guild.getAfkChannel();
         return afkChannel != null && afkChannel.equals(userState.getChannel()) ? 2 : 1;
      } else {
         return 0;
      }
   }

   public static Lyrics getLyrics(String title) {
      title = title.replaceAll("\\(.*\\)", "");
      title = title.replaceAll("\\[.*\\]", "");
      title = title.replaceAll("\\{.*\\}", "");
      String[] sources = new String[]{"A-Z Lyrics", "Genius", "MusixMatch", "LyricsFreak"};
      LyricsClient client = new LyricsClient();

      try {
         for (String source : sources) {
            Lyrics lyrics = (Lyrics)client.getLyrics(title, source).get();
            if (lyrics != null) {
               return lyrics;
            }
         }

         return null;
      } catch (Exception var8) {
         return null;
      }
   }

   public static boolean isRoleHierarchyLower(@NotNull List<Role> roles, @NotNull Role matchRole) {
      Checks.notNull(matchRole, "Match roles can not be null");
      return isRoleHierarchyLower(roles, matchRole.getPosition());
   }

   public static boolean isRoleHierarchyLower(@NotNull List<Role> roles, int hierarchyPosition) {
      for (Role role : roles) {
         if (role.getPosition() < hierarchyPosition) {
            return false;
         }
      }

      return true;
   }

   public static boolean isRoleHierarchyLower(@NotNull Role role, @NotNull Role roleToCompare) {
      return role.getPosition() < roleToCompare.getPosition();
   }

   public static Role getHighestFrom(@NotNull Member member) {
      Checks.notNull(member, "Member object can not be null");
      List<Role> roles = member.getRoles();
      return roles.isEmpty() ? null : roles.stream().sorted((first, second) -> {
         if (first.getPosition() == second.getPosition()) {
            return 0;
         } else {
            return first.getPosition() > second.getPosition() ? -1 : 1;
         }
      }).findFirst().orElseGet(null);
   }

   public static Role getHighestFrom(@NotNull List<Role> roles) {
      Checks.notNull(roles, "Member object can not be null");
      return roles.isEmpty() ? null : roles.stream().sorted((first, second) -> {
         if (first.getPosition() == second.getPosition()) {
            return 0;
         } else {
            return first.getPosition() > second.getPosition() ? -1 : 1;
         }
      }).findFirst().orElseGet(null);
   }

   public static boolean isValidURL(String urlString) {
      try {
         java.net.URI.create(urlString).toURL();
         return true;
      } catch (Exception var2) {
         return false;
      }
   }

   public static void sendAvatar(CommandInteraction event, Member member, StatsService statsService, LanguageService languageService, Bot bot) {
      sendAvatar(event, member.getUser(), member, statsService, languageService, bot);
   }

   public static void sendAvatar(CommandInteraction event, User user, Member member, StatsService statsService, LanguageService languageService, Bot bot) {
      String avatar = member != null ? member.getEffectiveAvatarUrl() + "?size=512" : user.getEffectiveAvatarUrl() + "?size=512";
      EmbedBuilder eb = new EmbedBuilder();
      eb.setDescription(languageService.getMessage("command.avatar.for", new Object[]{user.getAsMention()}));
      eb.setImage(avatar);
      if (member != null && member.getAvatarId() != null) {
         String uniqueId = UUID.randomUUID().toString().substring(0, 8);
         Button globalBtn = Button.secondary("avatar:global:" + uniqueId + ":" + user.getId(), languageService.getMessage("command.avatar.button.global"));
         Button serverBtn = Button.secondary("avatar:server:" + uniqueId + ":" + user.getId(), languageService.getMessage("command.avatar.button.server"));
         event.replyEmbeds(eb.build()).setComponents(ActionRow.of(globalBtn, serverBtn)).queue(hook -> {
            statsService.updateImagesSend(event.getGuild().getIdLong());
            waitForAvatarButton(hook, event.getUser().getIdLong(), user, member, languageService, bot, uniqueId);
         });
      } else {
         event.replyEmbeds(eb.build()).queue(success -> statsService.updateImagesSend(event.getGuild().getIdLong()));
      }
   }

   public static void sendAvatar(CommandEvent event, Member member, StatsService statsService, LanguageService languageService, Bot bot) {
      sendAvatar(event, member.getUser(), member, statsService, languageService, bot);
   }

   public static void sendAvatar(CommandEvent event, User user, Member member, StatsService statsService, LanguageService languageService, Bot bot) {
      String avatar = member != null ? member.getEffectiveAvatarUrl() + "?size=512" : user.getEffectiveAvatarUrl() + "?size=512";
      EmbedBuilder eb = new EmbedBuilder();
      eb.setDescription(languageService.getMessage("command.avatar.for", new Object[]{user.getAsMention()}));
      eb.setImage(avatar);
      if (member != null && member.getAvatarId() != null) {
         String uniqueId = UUID.randomUUID().toString().substring(0, 8);
         Button globalBtn = Button.secondary("avatar:global:" + uniqueId + ":" + user.getId(), languageService.getMessage("command.avatar.button.global"));
         Button serverBtn = Button.secondary("avatar:server:" + uniqueId + ":" + user.getId(), languageService.getMessage("command.avatar.button.server"));
         MessageCreateBuilder mb = new MessageCreateBuilder();
         mb.setEmbeds(eb.build());
         mb.setComponents(ActionRow.of(globalBtn, serverBtn));
         event.getChannel().sendMessage(mb.build()).queue(message -> {
            statsService.updateImagesSend(event.getGuild().getIdLong());
            waitForAvatarButton(message, event.getAuthor().getIdLong(), user, member, languageService, bot, uniqueId);
         });
      } else {
         event.reply(eb.build(), success -> statsService.updateImagesSend(event.getGuild().getIdLong()));
      }
   }

   private static void waitForAvatarButton(Object hookOrMessage, long authorId, User user, Member member, LanguageService languageService, Bot bot, String uniqueId) {
      String globalId = "avatar:global:" + uniqueId + ":" + user.getId();
      String serverId = "avatar:server:" + uniqueId + ":" + user.getId();

      bot.getWaiter().waitForEvent(ButtonInteractionEvent.class, (e) -> {
         return (e.getComponentId().equals(globalId) || e.getComponentId().equals(serverId)) && e.getUser().getIdLong() == authorId;
      }, (e) -> {
         String avatar = e.getComponentId().equals(globalId) ? user.getEffectiveAvatarUrl() + "?size=512" : member.getEffectiveAvatarUrl() + "?size=512";
         EmbedBuilder eb = new EmbedBuilder();
         eb.setDescription(languageService.getMessage("command.avatar.for", new Object[]{user.getAsMention()}));
         eb.setImage(avatar);
         e.editMessageEmbeds(eb.build()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
         waitForAvatarButton(hookOrMessage, authorId, user, member, languageService, bot, uniqueId);
      }, 30L, TimeUnit.SECONDS, () -> {
         if (hookOrMessage instanceof InteractionHook) {
            ((InteractionHook) hookOrMessage).editOriginalComponents(Collections.emptyList()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
         } else if (hookOrMessage instanceof Message) {
            ((Message) hookOrMessage).editMessageComponents(Collections.emptyList()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
         }
      });
   }


   public static String numtoString(int n) {
      String str;
      switch (n) {
         case 0:
            str = "0️⃣";
            break;
         case 1:
            str = "1️⃣";
            break;
         case 2:
            str = "2️⃣";
            break;
         case 3:
            str = "3️⃣";
            break;
         case 4:
            str = "4️⃣";
            break;
         case 5:
            str = "5️⃣";
            break;
         case 10:
            str = "🔟";
            break;
         case 6:
            str = "6️⃣";
            break;
         case 7:
            str = "7️⃣";
            break;
         case 8:
            str = "8️⃣";
            break;
         case 9:
            str = "9️⃣";
            break;
         default:
            str = "0️⃣";
      }

      return str;
   }
}

