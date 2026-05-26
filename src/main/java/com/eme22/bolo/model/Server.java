package com.eme22.bolo.model;

import com.eme22.bolo.settings.SettingsManager;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jagrosh.jdautilities.command.GuildSettingsProvider;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
@Entity(name = "embot_server_config")
@Transactional
public class Server extends PanacheEntityBase implements GuildSettingsProvider {

   @JsonIgnore
   @Transient
   private SettingsManager manager;

   @Id
   @Column(name = "server_id", nullable = false)
   private Long id;

   @Column(name = "server_musictextchanel")
   private long textChannelId;

   @Column(name = "server_musicvoicechanel")
   private long voiceChannelId;

   @Column(name = "server_djrole")
   private long djRoleId;

   @Column(name = "server_adminrole")
   private long adminRoleId;

   @Builder.Default
   @Column(name = "server_musicvolume")
   private int volume = 100;

   @Column(name = "server_defaultplaylistname")
   private String defaultPlaylist;

   @Column(name = "server_repeatmode")
   @Enumerated(EnumType.STRING)
   private RepeatMode repeatMode;

   @Column(name = "server_prefix")
   private String prefix;

   @Column(name = "server_musicskipratio")
   private double skipRatio;

   @Builder.Default
   @Column(name = "server_welcomechannelenabled")
   private Boolean bienvenidasChannelEnabled = false;

   @Column(name = "server_welcomechannelid")
   private long bienvenidasChannelId;

   @Column(name = "server_welcomechannelimage")
   private String bienvenidasChannelImage;

   @Column(name = "server_welcomechannelmessage")
   private String bienvenidasChannelMessage;

   @Builder.Default
   @Column(name = "server_goodbyechannelenabled")
   private Boolean despedidasChannelEnabled = false;

   @Column(name = "server_goodbyechannelid")
   private long despedidasChannelId;

   @Column(name = "server_goodbyechannelimage")
   private String despedidasChannelImage;

   @Column(name = "server_goodbyechannelmessage")
   private String despedidasChannelMessage;

   @Builder.Default
   @ElementCollection
   @CollectionTable(name = "embot_server_imagechannels")
   @Column(name = "image_only_channels_ids")
   private List<Long> imageOnlyChannelsIds = new ArrayList<>();

   @Builder.Default
   @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
   @JoinTable(name = "embot_server_memeimage", joinColumns = {
         @JoinColumn(name = "memeimagelist_id") }, inverseJoinColumns = { @JoinColumn(name = "meme_id") })
   private List<MemeImage> memeImages = new ArrayList<>();



   @Builder.Default
   @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
   @JoinTable(name = "embot_server_rolemanager", joinColumns = {
         @JoinColumn(name = "rolemanagerlist_id") }, inverseJoinColumns = { @JoinColumn(name = "role_id") })
   private List<RoleManager> roleManagerList = new ArrayList<>();

   @Builder.Default
   @ElementCollection
   @CollectionTable(name = "embot_server_8ballresponses")
   private List<String> eightBallAnswers = new ArrayList<>();

   @Builder.Default
   @OneToMany(cascade = { CascadeType.ALL })
   @JoinTable(name = "embot_server_birthday", joinColumns = {
         @JoinColumn(name = "birthdaylist_id") }, inverseJoinColumns = { @JoinColumn(name = "birthday_id") })
   private List<Birthday> birthdays = new ArrayList<>();

   @Column(name = "server_birthdaychannelid")
   private long birthdayChannelId;

   @Builder.Default
   @Column(name = "server_antiraidmode")
   private Boolean antiRaidMode = false;

   @Column(name = "server_songssend")
   private int songsSend;

   @Column(name = "server_usedtimes")
   private int usedTimes;

   @Column(name = "server_commandssend")
   private int commandsSend;

   @Column(name = "server_dateadded")
   private Date dateAdded;

   @Column(name = "server_datedismissed")
   private Date dateDismissed;

   @Column(name = "server_birthdaytemplateheader", nullable = true)
   @Nullable
   private String birthdayTemplateHeader;

   @Column(name = "server_birthdaytemplatefooter", nullable = true)
   @Nullable
   private String birthdayTemplateFooter;

   @Builder.Default
   @ElementCollection
   @CollectionTable(name = "embot_server_linkenhancerchannels")
   @Column(name = "server_linkenhancerchannels")
   private List<Long> linkEnhancerChannels = new ArrayList<>();

   @Builder.Default
   @Column(name = "server_linkenhancerenabled")
   private Boolean linkEnhancerEnabled = false;

   @Builder.Default
   @Column(name = "server_linkenhancers")
   @OneToMany(cascade = { CascadeType.ALL })
   @JoinTable(name = "embot_server_link_enhancer_rel", joinColumns = {
         @JoinColumn(name = "server_linkenhancer_id") }, inverseJoinColumns = { @JoinColumn(name = "server_id") })
   private List<LinkEnhancer> linkEnhancers = new ArrayList<>();

   @Column(name = "server_language")
   private String language;

   @Builder.Default
   @Column(name = "server_backupenabled", nullable = false)
   private boolean backupEnabled = false;

   @Builder.Default
   @Column(name = "server_ai_enabled", nullable = false)
   private boolean aiEnabled = false;

   @Builder.Default
   @Column(name = "server_ai_exclusive", nullable = false)
   private boolean aiExclusive = false;

   @Builder.Default
   @Column(name = "server_ai_channel_id")
   private Long aiChannelId = 0L;

    @Column(name = "server_ai_model")
   private String aiModel;

   @Column(name = "server_ai_base_url")
   private String aiBaseUrl;

   @Column(name = "server_ai_api_key")
   private String aiApiKey;

   public long getAiChannelId() {
      return this.aiChannelId == null ? 0L : this.aiChannelId;
   }

   @Override
   public int hashCode() {
      return this.getClass().hashCode();
   }



   public void deleteRoleManagers(long messageIdLong) {
      this.roleManagerList.removeIf(role -> role.getId().equals(messageIdLong));
   }

   public void addToEightBallAnswers(String answer) {
      this.eightBallAnswers.add(answer);
   }

   public void addOnlyImageChannels(TextChannel textChannel) {
      this.imageOnlyChannelsIds.add(textChannel.getIdLong());
   }

   public void addOnlyImageChannels(long textChannelId) {
      this.imageOnlyChannelsIds.add(textChannelId);
   }

   public void addToMemeImages(String message, String link) {
      this.memeImages.add(new MemeImage(0L, message, link));
   }

   public void removeFrom8BallAnswers(int answer) {
      this.eightBallAnswers.remove(answer);
   }

   public boolean isOnlyImageChannel(TextChannel textChannel) {
      return this.isOnlyImageChannel(textChannel.getIdLong());
   }

   public boolean isOnlyImageChannel(long textChannelId) {
      return this.imageOnlyChannelsIds.contains(textChannelId);
   }

   public void removeFromOnlyImageChannels(TextChannel textChannel) {
      this.removeFromOnlyImageChannels(textChannel.getIdLong());
   }

   public void removeFromOnlyImageChannels(long textChannelId) {
      this.imageOnlyChannelsIds.remove(textChannelId);
   }



   public void save() {
      this.manager.saveSettings(this);
   }

   public void deleteFromMemeImages(int i) {
      this.memeImages.remove(i);
   }

   public void addToRoleManagers(RoleManager manager) {
      this.roleManagerList.add(manager);
   }

   public RoleManager getRoleManager(long messageIdLong) {
      return this.roleManagerList.stream().filter(customer -> customer.getId().equals(messageIdLong)).findAny()
            .orElse(null);
   }

   public String getRandomAnswer() {
      return this.eightBallAnswers.get(new Random().nextInt(this.eightBallAnswers.size()));
   }

   public MemeImage getRandomMemeImages() {
      return this.memeImages.get(new Random().nextInt(this.memeImages.size()));
   }

   public void removeBirthDay(long idLong) {
      this.birthdays.removeIf(bd -> bd.getId().equals(idLong));
   }

   public void addBirthDay(Birthday cumple) {
      this.birthdays.add(cumple);
   }

   public Birthday getUserBirthday(long idLong) {
      return this.birthdays.stream().filter(user -> user.getUser().equals(idLong)).findAny().orElse(null);
   }

   public void addLinkEnhancer(LinkEnhancer linkEnhancer) {
      this.linkEnhancers.add(linkEnhancer);
   }

   public void addToLinkEnhancerChannels(long idLong) {
      this.linkEnhancerChannels.add(idLong);
   }

   public void removeFromLinkEnhancerChannels(long idLong) {
      this.linkEnhancerChannels.remove(idLong);
   }

   @Nullable
   @Override
   public Collection<String> getPrefixes() {
      return this.prefix == null ? null : Collections.singletonList(this.prefix);
   }
}
