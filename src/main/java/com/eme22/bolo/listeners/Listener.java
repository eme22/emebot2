package com.eme22.bolo.listeners;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.Bot;
import com.eme22.bolo.configuration.BotConfiguration;
import com.eme22.bolo.model.MusicArtWork;
import com.eme22.bolo.model.RoleManager;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.stats.StatsService;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageData;
import org.jetbrains.annotations.NotNull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.context.control.ActivateRequestContext;



@ApplicationScoped
@Slf4j
public class Listener implements EventListener {
   
   private final Bot bot;
   @ConfigProperty(name = "config.update")
   boolean updatealerts;
   @ConfigProperty(name = "config.owner")
   long owner;
   @ConfigProperty(name = "config.clientToken", defaultValue = "")
   String clientToken;
   
   @ConfigProperty(name = "quarkus.application.version")
   String version;
   
   private final StatsService statsService;
   private String setupMessage = null;
   private final HashMap<String, Integer> tempChannels = new HashMap<>();

   @Inject
   public Listener(Bot bot, StatsService statsService) {
      this.bot = bot;
      this.statsService = statsService;
   }

   private static String getDefaultCharSet() {
      OutputStreamWriter writer = new OutputStreamWriter(new ByteArrayOutputStream());
      return writer.getEncoding();
   }

   @Override
   public void onEvent(@NotNull GenericEvent event) {
      switch (event) {
         case ReadyEvent readyEvent -> this.onReady(readyEvent);
         case MessageReceivedEvent messageReceivedEvent -> this.onMessageReceived(messageReceivedEvent);
         case MessageDeleteEvent messageDeleteEvent -> this.onMessageDelete(messageDeleteEvent);
         case GuildVoiceUpdateEvent guildVoiceUpdateEvent -> this.onGuildVoiceUpdate(guildVoiceUpdateEvent);
         case ShutdownEvent shutdownEvent -> this.onShutdown(shutdownEvent);
         case GuildJoinEvent guildJoinEvent -> this.onGuildJoin(guildJoinEvent);
         case MessageReactionAddEvent messageReactionAddEvent -> this.onMessageReactionAdd(messageReactionAddEvent);
         case MessageReactionRemoveEvent messageReactionRemoveEvent -> this.onMessageReactionRemove(messageReactionRemoveEvent);
         case ButtonInteractionEvent buttonInteractionEvent -> this.onButtonInteraction(buttonInteractionEvent);
         case GuildMemberJoinEvent guildMemberJoinEvent -> this.onGuildMemberJoin(guildMemberJoinEvent);
         case GuildMemberRemoveEvent guildMemberRemoveEvent -> this.onGuildMemberRemove(guildMemberRemoveEvent);
         default -> {}
      }
   }

   @ActivateRequestContext
   @Transactional
   public void onReady(ReadyEvent event) {
      if (event.getJDA().getGuildCache().isEmpty()) {
         log.warn("This bot is not on any guilds! Use the following link to add the bot to your guilds!");
         log.warn(event.getJDA().getInviteUrl(BotConfiguration.RECOMMENDED_PERMS));
      }

      event.getJDA().getGuilds().forEach(guild -> {
         try {
            String defpl = this.bot.getSettingsManager().getSettings(guild).getDefaultPlaylist();
            VoiceChannel vc = guild.getVoiceChannelById(this.bot.getSettingsManager().getSettings(guild).getVoiceChannelId());
            if (defpl != null && vc != null) {
               this.bot.getPlayerManager().getAudioHandler(guild).playFromDefault();
            }
         } catch (Exception var4) {
         }
      });
      if (this.updatealerts) {
         this.bot
            .getThreadpool()
            .scheduleWithFixedDelay(
               () -> {
                  try {
                     User owner2 = (User)this.bot.getJDA().retrieveUserById(this.owner).complete();
                     String latestVersion = OtherUtil.getLatestVersion();
                     if (OtherUtil.compare(this.version, latestVersion) < 0) {
                        String msg = String.format(
                           "Hay una nueva version del bot!\nActual: %s\nNueva: %s\n\nVisite https://github.com/eme22/PGMUSICBOT/releases/latest para obtener la ultima version.",
                           this.version,
                           latestVersion
                        );
                        owner2.openPrivateChannel().queue(pc -> pc.sendMessage(msg).queue());
                     }
                  } catch (Exception var4) {
                  }
               },
               0L,
               24L,
               TimeUnit.HOURS
            );
      }
   }

   @ActivateRequestContext
   @Transactional
   public void onMessageReceived(@NotNull MessageReceivedEvent event) {
      if (!event.getAuthor().isBot()) {
         if (event.isFromGuild()) {
            Server s = this.bot.getSettingsManager().getSettings(event.getGuild());
            List<Long> bannedTextChannels = s.getImageOnlyChannelsIds();
            if (bannedTextChannels.contains(event.getChannel().getIdLong())) {
               Message message = event.getMessage();
               if (message.getContentRaw().contains("delimagechannel")) {
                  return;
               }

               if (message.getContentRaw().contains("https://")) {
                  return;
               }

               AtomicBoolean deletable = new AtomicBoolean(true);
               message.getEmbeds().forEach(messageEmbed -> {
                  if (messageEmbed.getImage() != null || messageEmbed.getVideoInfo() != null) {
                     deletable.set(false);
                  }
               });
               if (message.getAttachments().isEmpty() && deletable.get()) {
                  message.delete().complete();
               }
            }
         }
      }
   }

   @ActivateRequestContext
   @Transactional
   public void onMessageDelete(MessageDeleteEvent event) {
      if (event.isFromGuild()) {
         this.bot.getPlayerManager().getNowplayingHandler().onMessageDelete(event.getGuild(), event.getMessageIdLong());
      }
   }

   @ActivateRequestContext
   @Transactional
   public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
      this.bot.getAloneInVoiceHandler().onVoiceUpdate(event);
   }

   @ActivateRequestContext
   @Transactional
   public void onShutdown(@NotNull ShutdownEvent event) {
      this.bot.shutdown();
   }

   @ActivateRequestContext
   @Transactional
   public void onGuildJoin(GuildJoinEvent event) {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setDescription("¿Desea configurar el bot?");
      event.getGuild().getDefaultChannel().asTextChannel().sendMessageEmbeds(embedBuilder.build(), new MessageEmbed[0]).queue(message -> {
         this.setupMessage = message.getId();
         message.addReaction(Emoji.fromFormatted("U+2705")).queue();
         message.addReaction(Emoji.fromFormatted("U+274C")).queue();
      });
   }

   private void setupDefaultChannels(Guild guild) {
      try {
         Server s = this.bot.getSettingsManager().getSettings(guild);
         Long commandsChannel = s.getTextChannelId();
         Long bienvenidasChannel = s.getBienvenidasChannelId();
         Long despedidasChannel = s.getDespedidasChannelId();
         TextChannel defaultChannel = guild.getDefaultChannel().asTextChannel();
         List<TextChannel> channels = guild.getTextChannels();
         if (commandsChannel == null) {
            this.setupChannel("Comandos de Musica", defaultChannel, channels, 0);
         }

         if (bienvenidasChannel == null) {
            this.setupChannel("Bienvenidas", defaultChannel, channels, 1);
         }

         if (despedidasChannel == null) {
            this.setupChannel("Despedidas", defaultChannel, channels, 2);
         }
      } catch (Exception var8) {
         log.error("Error: " + var8.getMessage(), var8);
      }
   }

   private void setupChannel(String title, TextChannel defaultChannel, List<TextChannel> channels, int channel) {
      ArrayList<MessageCreateData> pages = new ArrayList<>();
      int calculatedPages = (int)Math.ceil(channels.size() / 10.0);

      for (int i = 1; i <= calculatedPages; i++) {
         StringBuilder sb = new StringBuilder();
         sb.append("Seleccione el canal para ").append(title).append(": \n");

         for (int j = (i - 1) * 10; j < Math.min(i * 10, channels.size()); j++) {
            sb.append(OtherUtil.numtoString(j)).append(" ").append(channels.get(j).getName()).append("\n");
         }

         MessageCreateBuilder msb = new MessageCreateBuilder();
         msb.setContent(sb.toString());
         pages.add(msb.build());
      }

      pages.forEach(page -> defaultChannel.sendMessage(page).queue(success -> {
         this.tempChannels.put(success.getId(), channel);

         for (int ix = 0; ix < this.getMessageItems(page); ix++) {
            success.addReaction(Emoji.fromFormatted("U+003" + ix + " U+FE0F U+20E3")).queue();
         }
      }));
   }

   private int getMessageItems(MessageData message) {
      String[] chans = message.getContent().split("\n");
      chans = Arrays.copyOfRange(chans, 1, chans.length);
      return chans.length;
   }

   @ActivateRequestContext
   @Transactional
   public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
      if (!event.getUser().isBot()) {
         if (event.isFromGuild()) {
            if (this.setupMessage != null && this.setupMessage.equals(event.getMessageId())) {
               if (event.getReaction().getEmoji().getName().contains("white_check_mark")) {
                  this.setupDefaultChannels(event.getGuild());
                  return;
               }

               ((Message)event.retrieveMessage().complete()).delete().complete();
            }

            if (!this.tempChannels.containsKey(event.getMessageId())) {
               RoleManager manager = this.bot.getSettingsManager().getSettings(event.getGuild().getIdLong()).getRoleManager(event.getMessageIdLong());
               if (manager != null) {
                  String reaction = event.getReaction().getEmoji().getAsReactionCode();
                  if (manager.isToggled()) {
                     List<MessageReaction> reactionsList = ((Message)event.getChannel().asTextChannel().retrieveMessageById(event.getMessageId()).complete())
                        .getReactions();
                     reactionsList.forEach(messageReaction -> {
                        List<User> users = (List<User>)messageReaction.retrieveUsers().complete();
                        users.forEach(user -> {
                           if (user.equals(event.getUser()) && !event.getReaction().getEmoji().equals(messageReaction.getEmoji())) {
                              messageReaction.removeReaction(user).complete();
                           }
                        });
                     });
                  }

                  Map<String, String> data = manager.getEmoji();
                  if (data.containsKey(event.getReaction().getEmoji().getAsReactionCode())) {
                     String roleT = data.get(reaction);
                     List<Role> list = FinderUtil.findRoles(roleT, event.getGuild());
                     event.getGuild().addRoleToMember(event.getMember(), list.get(0)).queue();
                  }
               }
            } else {
               String reactionx = event.getReaction().getEmoji().getName();
               int channel = Integer.parseInt(reactionx.replaceAll("[^\\d.]", ""));
               TextChannel channelId = this.getChannelFromMessage(channel, (Message)event.retrieveMessage().complete());
               if (channelId != null) {
                  int mode = this.tempChannels.get(event.getMessageId());
                  if (mode == 0) {
                     for (String key : getKeys(this.tempChannels, 0)) {
                        Message msgToDelete = (Message)event.getChannel().asTextChannel().retrieveMessageById(key).complete();
                        msgToDelete.delete().complete();
                     }

                     this.bot.getSettingsManager().getSettings(event.getGuild()).setTextChannelId(channelId.getIdLong());
                  }

                  if (mode == 1) {
                     for (String key : getKeys(this.tempChannels, 1)) {
                        Message msgToDelete = (Message)event.getChannel().asTextChannel().retrieveMessageById(key).complete();
                        msgToDelete.delete().complete();
                     }

                     this.bot.getSettingsManager().getSettings(event.getGuild()).setBienvenidasChannelId(channelId.getIdLong());
                  }

                  if (mode == 2) {
                     for (String key : getKeys(this.tempChannels, 2)) {
                        Message msgToDelete = (Message)event.getChannel().asTextChannel().retrieveMessageById(key).complete();
                        msgToDelete.delete().complete();
                     }

                     this.bot.getSettingsManager().getSettings(event.getGuild()).setDespedidasChannelId(channelId.getIdLong());
                  }
               }
            }
         }
      }
   }

   @ActivateRequestContext
   @Transactional
   public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
      if (!event.getUser().isBot()) {
         if (event.isFromGuild()) {
            RoleManager manager = this.bot.getSettingsManager().getSettings(event.getGuild().getIdLong()).getRoleManager(event.getMessageIdLong());
            if (manager != null) {
               String reaction = event.getReaction().getEmoji().getAsReactionCode();
               Map<String, String> datas = manager.getEmoji();
               if (datas.containsKey(reaction)) {
                  List<Role> list = FinderUtil.findRoles(datas.get(reaction), event.getGuild());
                  event.getGuild().removeRoleFromMember(event.getMember(), list.get(0)).complete();
               }
            }
         }
      }
   }

   private TextChannel getChannelFromMessage(int channel, Message message) {
      String[] chans = message.getContentRaw().split("\n");
      chans = Arrays.copyOfRange(chans, 1, chans.length);
      if (channel > chans.length) {
         return null;
      } else {
         String channam = chans[channel].split(":")[2].substring(1);
         return (TextChannel)message.getGuild().getTextChannelsByName(channam, true).get(0);
      }
   }

   private static Set<String> getKeys(Map<String, Integer> map, Integer value) {
      Set<String> result = new HashSet<>();
      if (map.containsValue(value)) {
         for (Entry<String, Integer> entry : map.entrySet()) {
            if (Objects.equals(entry.getValue(), value)) {
               result.add(entry.getKey());
            }
         }
      }

      return result;
   }

   @ActivateRequestContext
   @Transactional
   public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
      String id = event.getComponentId();
      switch (id) {
         case "acceptArtWork":
            List<MessageEmbed> embeds = event.getMessage().getEmbeds();
            MusicArtWork artWork = new MusicArtWork();
            artWork.setArtist(embeds.get(0).getDescription());
            artWork.setUrl(embeds.get(1).getDescription());
            artWork.setSubmitedBy(event.getUser().getIdLong());
            this.bot.getArtworkImageService().addArtWork(artWork);
            event.reply("Se ha agregado correctamente la imagen al bot.").queue(event2 -> event.editButton(event.getButton().asDisabled()).queue());
            break;
         case "rejectArtWork":
            event.reply("Realizado.").queue(message -> event.getMessage().delete().queue(event2 -> event.editButton(event.getButton().asDisabled()).queue()));
      }
   }

   @ActivateRequestContext
   @Transactional
   public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
      Guild guild = event.getGuild();
      User member = event.getMember().getUser();
      log.info("onGuildMemberJoin triggered for user: {} ({}) in guild: {}", member.getName(), member.getId(), guild.getName());

      try {
         if (!this.bot.getSettingsManager().getSettings(guild).getBienvenidasChannelEnabled()) {
            log.info("Welcome messages are disabled for guild: {}", guild.getName());
            return;
         }

         long channelId = this.bot.getSettingsManager().getSettings(guild).getBienvenidasChannelId();
         TextChannel bienvenidas = guild.getTextChannelById(channelId);
         if (bienvenidas != null) {
            log.info("Found welcome channel: {} ({})", bienvenidas.getName(), channelId);
            InputStream bienvenida = OtherUtil.getBackground(this.bot.getSettingsManager().getSettings(guild), true, this.clientToken);
            String userImage = this.getUserImage(member);
            File converted = this.getMemberFile(member);
            log.info("Generating welcome image for user: {}", member.getName());
            try {
                OtherUtil.createImage("BIENVENIDO", member.getName(), member.getId(), bienvenida, userImage, converted, this.clientToken);
            } catch (Exception e) {
                log.error("Failed to generate welcome image: " + e.getMessage(), e);
            }

            if (!converted.exists()) {
               log.error("Image not created at path: {}", converted.getAbsolutePath());
            } else {
               log.info("Image successfully created at path: {}", converted.getAbsolutePath());
            }

            String message = OtherUtil.getMessage(this.bot, guild, true);
            if (member.isBot()) {
               message = "Un bot ha llegado";
            }

            log.info("Sending welcome message to channel: {}", bienvenidas.getName());
            this.sendMessage(guild, member, bienvenidas, converted, message);
         } else {
            log.error("Welcome channel not found for ID: {}", channelId);
         }
      } catch (Exception var9) {
         log.error("Error in onGuildMemberJoin: " + var9.getMessage(), var9);
      }
   }

   private String getUserImage(User member) {
      String userImage = member.getAvatarUrl();
      if (userImage == null) {
         userImage = member.getDefaultAvatarUrl();
      } else {
         userImage = member.getAvatarUrl();
      }

      return userImage;
   }

   @ActivateRequestContext
   @Transactional
   public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
      Guild guild = event.getGuild();
      User member = event.getMember().getUser();
      log.info("onGuildMemberRemove triggered for user: {} ({}) in guild: {}", member.getName(), member.getId(), guild.getName());

      try {
         if (!this.bot.getSettingsManager().getSettings(guild).getDespedidasChannelEnabled()) {
            log.info("Farewell messages are disabled for guild: {}", guild.getName());
            return;
         }

         long channelId = this.bot.getSettingsManager().getSettings(guild).getDespedidasChannelId();
         TextChannel despedidas = guild.getTextChannelById(channelId);
         if (despedidas != null) {
            log.info("Found farewell channel: {} ({})", despedidas.getName(), channelId);
            InputStream despedida = OtherUtil.getBackground(this.bot.getSettingsManager().getSettings(guild), false, this.clientToken);
            String userImage = this.getUserImage(member);
            File converted = this.getMemberFile(member);
            log.info("Generating farewell image for user: {}", member.getName());
            try {
                OtherUtil.createImage("SE VA", member.getName(), member.getId(), despedida, userImage, converted, this.clientToken);
            } catch (Exception e) {
                log.error("Failed to generate farewell image: " + e.getMessage(), e);
            }

            if (!converted.exists()) {
               log.error("Image not created at path: {}", converted.getAbsolutePath());
            } else {
               log.info("Image successfully created at path: {}", converted.getAbsolutePath());
            }

            String message = OtherUtil.getMessage(this.bot, guild, false);
            log.info("Sending farewell message to channel: {}", despedidas.getName());
            this.sendMessage(guild, member, despedidas, converted, message);
         } else {
            log.error("Farewell channel not found for ID: {}", channelId);
         }
      } catch (Exception var9) {
         log.error("Error in onGuildMemberRemove: " + var9.getMessage(), var9);
      }
   }

   private void sendMessage(Guild guild, User member, TextChannel channel, File converted, String message) {
      if (message == null || message.trim().isEmpty()) {
         message = member.getName() + " " + (converted.exists() ? "" : "ha llegado/se ha ido");
      }

      message = message.replaceAll("@username", member.getAsMention())
         .replaceAll("@servername", guild.getName())
         .replaceAll("@channel", channel.getAsMention());
      
      if (message.trim().isEmpty()) {
          message = member.getAsMention();
      }
      
      if (converted.exists()) {
          ((MessageCreateAction)channel.sendMessage(message).addFiles(new FileUpload[]{FileUpload.fromData(converted)})).queue(success -> {
             log.info("Message successfully sent to channel: {}", channel.getName());
             this.statsService.increment(success.getGuild().getIdLong(), "IMAGES_SEND");
             if (converted.delete()) {
                log.info("Temporary image file deleted: {}", converted.getName());
             } else {
                log.warn("Failed to delete temporary image file: {}", converted.getName());
             }
          }, throwable -> {
             log.error("Failed to send welcome/farewell message: " + throwable.getMessage(), throwable);
          });
      } else {
          log.warn("Image file not found, sending text only message: {}", converted.getAbsolutePath());
          channel.sendMessage(message).queue(success -> {
              log.info("Text-only message successfully sent to channel: {}", channel.getName());
          }, throwable -> {
              log.error("Failed to send text-only welcome/farewell message: " + throwable.getMessage(), throwable);
          });
      }
   }

   @NotNull
   private File getMemberFile(User member) {
      File parent = new File(System.getProperty("java.io.tmpdir"), "emebot-temp").getAbsoluteFile();
      if (!parent.exists()) {
         if (parent.mkdirs()) {
            log.info("Temp folder successfully created: {}", parent.getAbsolutePath());
         } else {
            log.error("Failed to create temp folder: {}", parent.getAbsolutePath());
         }
      }

      File converted = new File(parent, member.getId() + ".png");
      if (converted.exists()) {
         if (converted.delete()) {
            log.info("Existing temporary image file deleted: {}", converted.getName());
         } else {
            log.warn("Failed to delete existing temporary image file: {}", converted.getName());
         }
      }

      return converted;
   }
}
