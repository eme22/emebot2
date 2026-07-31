package com.eme22.bolo.dto;

import com.eme22.bolo.model.MemeImage;
import com.eme22.bolo.model.RepeatMode;
import com.eme22.bolo.model.RoleManager;
import com.eme22.bolo.model.Server;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerConfigDTO {
    private Long id;
    private Long textChannelId;
    private Long voiceChannelId;
    private Long djRoleId;
    private Long adminRoleId;
    private Integer volume;
    private String defaultPlaylist;
    private RepeatMode repeatMode;
    private String prefix;
    private Double skipRatio;
    private Boolean bienvenidasChannelEnabled;
    private Long bienvenidasChannelId;
    private String bienvenidasChannelImage;
    private String bienvenidasChannelMessage;
    private Boolean despedidasChannelEnabled;
    private Long despedidasChannelId;
    private String despedidasChannelImage;
    private String despedidasChannelMessage;
    private List<Long> imageOnlyChannelsIds;
    private List<String> eightBallAnswers;
    private Long birthdayChannelId;
    private String birthdayTemplateHeader;
    private String birthdayTemplateFooter;
    private Boolean antiRaidMode;
    private Boolean linkEnhancerEnabled;
    private List<Long> linkEnhancerChannels;
    private String language;
    private Boolean backupEnabled;
    private Boolean aiEnabled;
    private Boolean aiExclusive;
    private Long aiChannelId;
    private String aiModel;
    private String aiBaseUrl;
    private String aiApiKey;
    private List<MemeImageDTO> memeImages;
    private List<RoleManagerDTO> roleManagerList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MemeImageDTO {
        private String message;
        private String meme;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoleManagerDTO {
        private Long id;
        private Map<String, String> emoji;
        private boolean toggled;
    }

    public static ServerConfigDTO fromEntity(Server server, boolean maskApiKey) {
        if (server == null) return null;

        List<MemeImageDTO> memes = server.getMemeImages() == null ? new ArrayList<>() :
                server.getMemeImages().stream()
                        .map(m -> new MemeImageDTO(m.getMessage(), m.getMeme()))
                        .collect(Collectors.toList());

        List<RoleManagerDTO> roles = server.getRoleManagerList() == null ? new ArrayList<>() :
                server.getRoleManagerList().stream()
                        .map(r -> new RoleManagerDTO(r.getId(), r.getEmoji(), r.isToggled()))
                        .collect(Collectors.toList());

        return ServerConfigDTO.builder()
                .id(server.getId())
                .textChannelId(server.getTextChannelId())
                .voiceChannelId(server.getVoiceChannelId())
                .djRoleId(server.getDjRoleId())
                .adminRoleId(server.getAdminRoleId())
                .volume(server.getVolume())
                .defaultPlaylist(server.getDefaultPlaylist())
                .repeatMode(server.getRepeatMode())
                .prefix(server.getPrefix())
                .skipRatio(server.getSkipRatio())
                .bienvenidasChannelEnabled(server.getBienvenidasChannelEnabled())
                .bienvenidasChannelId(server.getBienvenidasChannelId())
                .bienvenidasChannelImage(server.getBienvenidasChannelImage())
                .bienvenidasChannelMessage(server.getBienvenidasChannelMessage())
                .despedidasChannelEnabled(server.getDespedidasChannelEnabled())
                .despedidasChannelId(server.getDespedidasChannelId())
                .despedidasChannelImage(server.getDespedidasChannelImage())
                .despedidasChannelMessage(server.getDespedidasChannelMessage())
                .imageOnlyChannelsIds(server.getImageOnlyChannelsIds() != null ? new ArrayList<>(server.getImageOnlyChannelsIds()) : new ArrayList<>())
                .eightBallAnswers(server.getEightBallAnswers() != null ? new ArrayList<>(server.getEightBallAnswers()) : new ArrayList<>())
                .birthdayChannelId(server.getBirthdayChannelId())
                .birthdayTemplateHeader(server.getBirthdayTemplateHeader())
                .birthdayTemplateFooter(server.getBirthdayTemplateFooter())
                .antiRaidMode(server.getAntiRaidMode())
                .linkEnhancerEnabled(server.getLinkEnhancerEnabled())
                .linkEnhancerChannels(server.getLinkEnhancerChannels() != null ? new ArrayList<>(server.getLinkEnhancerChannels()) : new ArrayList<>())
                .language(server.getLanguage())
                .backupEnabled(server.isBackupEnabled())
                .aiEnabled(server.isAiEnabled())
                .aiExclusive(server.isAiExclusive())
                .aiChannelId(server.getAiChannelId())
                .aiModel(server.getAiModel())
                .aiBaseUrl(server.getAiBaseUrl())
                .aiApiKey(maskApiKey ? "" : server.getAiApiKey())
                .memeImages(memes)
                .roleManagerList(roles)
                .build();
    }

    public void mergeIntoEntity(Server server) {
        if (server == null) return;

        if (this.textChannelId != null) server.setTextChannelId(this.textChannelId);
        if (this.voiceChannelId != null) server.setVoiceChannelId(this.voiceChannelId);
        if (this.djRoleId != null) server.setDjRoleId(this.djRoleId);
        if (this.adminRoleId != null) server.setAdminRoleId(this.adminRoleId);
        if (this.volume != null) server.setVolume(this.volume);
        if (this.defaultPlaylist != null) server.setDefaultPlaylist(this.defaultPlaylist);
        if (this.repeatMode != null) server.setRepeatMode(this.repeatMode);
        if (this.prefix != null) server.setPrefix(this.prefix);
        if (this.skipRatio != null) server.setSkipRatio(this.skipRatio);
        if (this.bienvenidasChannelEnabled != null) server.setBienvenidasChannelEnabled(this.bienvenidasChannelEnabled);
        if (this.bienvenidasChannelId != null) server.setBienvenidasChannelId(this.bienvenidasChannelId);
        if (this.bienvenidasChannelImage != null) server.setBienvenidasChannelImage(this.bienvenidasChannelImage);
        if (this.bienvenidasChannelMessage != null) server.setBienvenidasChannelMessage(this.bienvenidasChannelMessage);
        if (this.despedidasChannelEnabled != null) server.setDespedidasChannelEnabled(this.despedidasChannelEnabled);
        if (this.despedidasChannelId != null) server.setDespedidasChannelId(this.despedidasChannelId);
        if (this.despedidasChannelImage != null) server.setDespedidasChannelImage(this.despedidasChannelImage);
        if (this.despedidasChannelMessage != null) server.setDespedidasChannelMessage(this.despedidasChannelMessage);

        if (this.imageOnlyChannelsIds != null) {
            server.setImageOnlyChannelsIds(new ArrayList<>(this.imageOnlyChannelsIds));
        }
        if (this.eightBallAnswers != null) {
            server.setEightBallAnswers(new ArrayList<>(this.eightBallAnswers));
        }
        if (this.birthdayChannelId != null) server.setBirthdayChannelId(this.birthdayChannelId);
        if (this.birthdayTemplateHeader != null) server.setBirthdayTemplateHeader(this.birthdayTemplateHeader);
        if (this.birthdayTemplateFooter != null) server.setBirthdayTemplateFooter(this.birthdayTemplateFooter);
        if (this.antiRaidMode != null) server.setAntiRaidMode(this.antiRaidMode);
        if (this.linkEnhancerEnabled != null) server.setLinkEnhancerEnabled(this.linkEnhancerEnabled);
        if (this.linkEnhancerChannels != null) {
            server.setLinkEnhancerChannels(new ArrayList<>(this.linkEnhancerChannels));
        }
        if (this.language != null) server.setLanguage(this.language);
        if (this.backupEnabled != null) server.setBackupEnabled(this.backupEnabled);
        if (this.aiEnabled != null) server.setAiEnabled(this.aiEnabled);
        if (this.aiExclusive != null) server.setAiExclusive(this.aiExclusive);
        if (this.aiChannelId != null) server.setAiChannelId(this.aiChannelId);
        if (this.aiModel != null) server.setAiModel(this.aiModel);
        if (this.aiBaseUrl != null) server.setAiBaseUrl(this.aiBaseUrl);
        if (this.aiApiKey != null && !this.aiApiKey.isEmpty()) server.setAiApiKey(this.aiApiKey);

        if (this.memeImages != null && !this.memeImages.isEmpty()) {
            List<MemeImage> memes = this.memeImages.stream()
                    .map(m -> new MemeImage(0L, m.getMessage(), m.getMeme()))
                    .collect(Collectors.toList());
            server.setMemeImages(memes);
        }

        if (this.roleManagerList != null && !this.roleManagerList.isEmpty()) {
            List<RoleManager> roles = this.roleManagerList.stream()
                    .map(r -> {
                        RoleManager rm = new RoleManager();
                        rm.setId(r.getId());
                        rm.setEmoji(r.getEmoji());
                        rm.setToggled(r.isToggled());
                        return rm;
                    })
                    .collect(Collectors.toList());
            server.setRoleManagerList(roles);
        }
    }
}
