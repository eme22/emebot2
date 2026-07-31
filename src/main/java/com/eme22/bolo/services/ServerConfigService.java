package com.eme22.bolo.services;

import com.eme22.bolo.dto.ServerConfigDTO;
import com.eme22.bolo.dto.ValidationReport;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.repository.ServerRepository;
import com.eme22.bolo.settings.SettingsManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class ServerConfigService {

    private final SettingsManager settingsManager;
    private final ServerRepository serverRepository;
    private final ObjectMapper objectMapper;

    @Inject
    public ServerConfigService(SettingsManager settingsManager, ServerRepository serverRepository, ObjectMapper objectMapper) {
        this.settingsManager = settingsManager;
        this.serverRepository = serverRepository;
        this.objectMapper = objectMapper.copy()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public String exportServerConfigJson(Server server) throws JsonProcessingException {
        ServerConfigDTO dto = ServerConfigDTO.fromEntity(server, true);
        return objectMapper.writeValueAsString(dto);
    }

    public String exportAllServerConfigsJson() throws JsonProcessingException {
        List<Server> servers = serverRepository.listAll();
        List<ServerConfigDTO> dtoList = servers.stream()
                .map(server -> ServerConfigDTO.fromEntity(server, false))
                .collect(Collectors.toList());
        return objectMapper.writeValueAsString(dtoList);
    }

    public ServerConfigDTO parseServerConfigJson(String jsonContent) throws JsonProcessingException {
        return objectMapper.readValue(jsonContent, ServerConfigDTO.class);
    }

    public List<ServerConfigDTO> parseAllServerConfigsJson(String jsonContent) throws JsonProcessingException {
        return objectMapper.readValue(jsonContent, new TypeReference<List<ServerConfigDTO>>() {});
    }

    public ValidationReport validateServerConfig(Guild guild, ServerConfigDTO dto, LanguageService lang) {
        ValidationReport report = ValidationReport.builder().valid(true).build();

        if (dto == null) {
            String err = lang != null ? lang.getMessage("validation.error.deserialization") : "El contenido JSON no pudo ser deserializado.";
            report.addError(err);
            return report;
        }

        // Field presence reporting & Discord entity checks
        if (dto.getTextChannelId() != null) {
            report.addUpdatedField("textChannelId");
            if (guild != null && dto.getTextChannelId() > 0 && guild.getTextChannelById(dto.getTextChannelId()) == null) {
                String warn = lang != null ? lang.getMessage("validation.warning.textchannel", dto.getTextChannelId()) : "Canal de texto de música ID " + dto.getTextChannelId() + " no existe en el servidor.";
                report.addWarning(warn);
            }
        }
        if (dto.getVoiceChannelId() != null) {
            report.addUpdatedField("voiceChannelId");
            if (guild != null && dto.getVoiceChannelId() > 0 && guild.getVoiceChannelById(dto.getVoiceChannelId()) == null) {
                String warn = lang != null ? lang.getMessage("validation.warning.voicechannel", dto.getVoiceChannelId()) : "Canal de voz de música ID " + dto.getVoiceChannelId() + " no existe en el servidor.";
                report.addWarning(warn);
            }
        }
        if (dto.getAdminRoleId() != null) {
            report.addUpdatedField("adminRoleId");
            if (guild != null && dto.getAdminRoleId() > 0 && guild.getRoleById(dto.getAdminRoleId()) == null) {
                String warn = lang != null ? lang.getMessage("validation.warning.adminrole", dto.getAdminRoleId()) : "Rol de admin ID " + dto.getAdminRoleId() + " no existe en el servidor.";
                report.addWarning(warn);
            }
        }
        if (dto.getDjRoleId() != null) {
            report.addUpdatedField("djRoleId");
            if (guild != null && dto.getDjRoleId() > 0 && guild.getRoleById(dto.getDjRoleId()) == null) {
                String warn = lang != null ? lang.getMessage("validation.warning.djrole", dto.getDjRoleId()) : "Rol DJ ID " + dto.getDjRoleId() + " no existe en el servidor.";
                report.addWarning(warn);
            }
        }
        if (dto.getBienvenidasChannelId() != null) {
            report.addUpdatedField("bienvenidasChannelId");
            if (guild != null && dto.getBienvenidasChannelId() > 0 && guild.getTextChannelById(dto.getBienvenidasChannelId()) == null) {
                String warn = lang != null ? lang.getMessage("validation.warning.welcomechannel", dto.getBienvenidasChannelId()) : "Canal de bienvenida ID " + dto.getBienvenidasChannelId() + " no existe en el servidor.";
                report.addWarning(warn);
            }
        }
        if (dto.getDespedidasChannelId() != null) {
            report.addUpdatedField("despedidasChannelId");
            if (guild != null && dto.getDespedidasChannelId() > 0 && guild.getTextChannelById(dto.getDespedidasChannelId()) == null) {
                String warn = lang != null ? lang.getMessage("validation.warning.goodbyechannel", dto.getDespedidasChannelId()) : "Canal de despedidas ID " + dto.getDespedidasChannelId() + " no existe en el servidor.";
                report.addWarning(warn);
            }
        }
        if (dto.getBirthdayChannelId() != null) {
            report.addUpdatedField("birthdayChannelId");
            if (guild != null && dto.getBirthdayChannelId() > 0 && guild.getTextChannelById(dto.getBirthdayChannelId()) == null) {
                String warn = lang != null ? lang.getMessage("validation.warning.birthdaychannel", dto.getBirthdayChannelId()) : "Canal de cumpleaños ID " + dto.getBirthdayChannelId() + " no existe en el servidor.";
                report.addWarning(warn);
            }
        }
        if (dto.getAiChannelId() != null) {
            report.addUpdatedField("aiChannelId");
            if (guild != null && dto.getAiChannelId() > 0 && guild.getTextChannelById(dto.getAiChannelId()) == null) {
                String warn = lang != null ? lang.getMessage("validation.warning.aichannel", dto.getAiChannelId()) : "Canal de IA ID " + dto.getAiChannelId() + " no existe en el servidor.";
                report.addWarning(warn);
            }
        }
        if (dto.getImageOnlyChannelsIds() != null && !dto.getImageOnlyChannelsIds().isEmpty()) {
            report.addUpdatedField("imageOnlyChannelsIds");
            if (guild != null) {
                for (Long cid : dto.getImageOnlyChannelsIds()) {
                    if (cid > 0 && guild.getTextChannelById(cid) == null) {
                        String warn = lang != null ? lang.getMessage("validation.warning.imagechannel", cid) : "Canal de solo imágenes ID " + cid + " no existe en el servidor.";
                        report.addWarning(warn);
                    }
                }
            }
        }
        if (dto.getLinkEnhancerChannels() != null && !dto.getLinkEnhancerChannels().isEmpty()) {
            report.addUpdatedField("linkEnhancerChannels");
            if (guild != null) {
                for (Long cid : dto.getLinkEnhancerChannels()) {
                    if (cid > 0 && guild.getTextChannelById(cid) == null) {
                        String warn = lang != null ? lang.getMessage("validation.warning.linkenhancerchannel", cid) : "Canal de mejora de enlaces ID " + cid + " no existe en el servidor.";
                        report.addWarning(warn);
                    }
                }
            }
        }

        if (dto.getVolume() != null) report.addUpdatedField("volume");
        if (dto.getPrefix() != null) report.addUpdatedField("prefix");
        if (dto.getLanguage() != null) report.addUpdatedField("language");
        if (dto.getRepeatMode() != null) report.addUpdatedField("repeatMode");
        if (dto.getSkipRatio() != null) report.addUpdatedField("skipRatio");
        if (dto.getBienvenidasChannelEnabled() != null) report.addUpdatedField("bienvenidasChannelEnabled");
        if (dto.getBienvenidasChannelImage() != null) report.addUpdatedField("bienvenidasChannelImage");
        if (dto.getBienvenidasChannelMessage() != null) report.addUpdatedField("bienvenidasChannelMessage");
        if (dto.getDespedidasChannelEnabled() != null) report.addUpdatedField("despedidasChannelEnabled");
        if (dto.getDespedidasChannelImage() != null) report.addUpdatedField("despedidasChannelImage");
        if (dto.getDespedidasChannelMessage() != null) report.addUpdatedField("despedidasChannelMessage");
        if (dto.getEightBallAnswers() != null) report.addUpdatedField("eightBallAnswers");
        if (dto.getAntiRaidMode() != null) report.addUpdatedField("antiRaidMode");
        if (dto.getLinkEnhancerEnabled() != null) report.addUpdatedField("linkEnhancerEnabled");
        if (dto.getAiEnabled() != null) report.addUpdatedField("aiEnabled");
        if (dto.getAiExclusive() != null) report.addUpdatedField("aiExclusive");
        if (dto.getAiModel() != null) report.addUpdatedField("aiModel");
        if (dto.getAiBaseUrl() != null) report.addUpdatedField("aiBaseUrl");
        if (dto.getMemeImages() != null) report.addUpdatedField("memeImages");
        if (dto.getRoleManagerList() != null) report.addUpdatedField("roleManagerList");

        return report;
    }

    @Transactional
    public void importServerConfig(long guildId, ServerConfigDTO dto) {
        Server server = settingsManager.getSettings(guildId);
        dto.mergeIntoEntity(server);
        settingsManager.saveSettings(server);
    }

    @Transactional
    public int importAllServerConfigs(List<ServerConfigDTO> dtoList) {
        int count = 0;
        for (ServerConfigDTO dto : dtoList) {
            if (dto.getId() == null || dto.getId() <= 0) continue;
            Server server = settingsManager.getSettings(dto.getId());
            dto.mergeIntoEntity(server);
            settingsManager.saveSettings(server);
            count++;
        }
        return count;
    }
}
