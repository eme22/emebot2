package com.eme22.bolo.commands.owner;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.OwnerCommand;
import com.eme22.bolo.dto.ServerConfigDTO;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.services.ServerConfigService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Singleton
@Slf4j
@Transactional
@ActivateRequestContext
public class AllServerConfigCmd extends OwnerCommand {

    @ConfigProperty(name = "config.aliases.allserverconfig", defaultValue = "")
    protected String[] aliases;

    private final Bot bot;

    @Inject
    public AllServerConfigCmd(ServerConfigService configService, Bot bot) {
        super();
        this.bot = bot;
        this.name = "allserverconfig";
        this.nameLocalization = Map.of(
                DiscordLocale.ENGLISH_UK, "allserverconfig",
                DiscordLocale.ENGLISH_US, "allserverconfig",
                DiscordLocale.SPANISH, "configtodosservidores",
                DiscordLocale.SPANISH_LATAM, "configtodosservidores"
        );
        this.help = "Copia de seguridad y restauración global en JSON de todos los servidores (Solo Owner).";
        this.descriptionLocalization = Map.of(
                DiscordLocale.ENGLISH_UK, "Global JSON backup and restore for all servers (Owner only)",
                DiscordLocale.ENGLISH_US, "Global JSON backup and restore for all servers (Owner only)",
                DiscordLocale.SPANISH, "Copia de seguridad y restauración global en JSON de todos los servidores (Solo Owner)",
                DiscordLocale.SPANISH_LATAM, "Copia de seguridad y restauración global en JSON de todos los servidores (Solo Owner)"
        );
        this.guildOnly = false;
        this.children = new SlashCommand[]{
                new ExportSubCmd(configService, bot),
                new ImportSubCmd(configService, bot)
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        LanguageService lang = bot.getSettingsManager().getLanguageService(event.getGuild());
        event.reply(lang.getMessage("command.allserverconfig.subcommand.required")).setEphemeral(true).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        LanguageService lang = bot.getSettingsManager().getLanguageService(event.getGuild());
        String[] parts = event.getArgs().split("\\s+", 2);
        String sub = parts[0].toLowerCase();
        if ("export".equals(sub)) {
            ((ExportSubCmd) this.children[0]).execute(event);
        } else if ("import".equals(sub)) {
            ((ImportSubCmd) this.children[1]).execute(event);
        } else {
            event.replyError(lang.getMessage("command.allserverconfig.usage", event.getClient().getPrefix()));
        }
    }

    public static class ExportSubCmd extends OwnerCommand {
        private final ServerConfigService configService;
        private final Bot bot;

        public ExportSubCmd(ServerConfigService configService, Bot bot) {
            super();
            this.configService = configService;
            this.bot = bot;
            this.name = "export";
            this.nameLocalization = Map.of(
                    DiscordLocale.ENGLISH_UK, "export",
                    DiscordLocale.ENGLISH_US, "export",
                    DiscordLocale.SPANISH, "exportar",
                    DiscordLocale.SPANISH_LATAM, "exportar"
            );
            this.help = "Exporta la configuración completa de TODOS los servidores en JSON sin enmascaramiento.";
            this.descriptionLocalization = Map.of(
                    DiscordLocale.ENGLISH_UK, "Export full configuration of ALL servers in JSON without masking",
                    DiscordLocale.ENGLISH_US, "Export full configuration of ALL servers in JSON without masking",
                    DiscordLocale.SPANISH, "Exporta la configuración completa de TODOS los servidores en JSON sin enmascaramiento",
                    DiscordLocale.SPANISH_LATAM, "Exporta la configuración completa de TODOS los servidores en JSON sin enmascaramiento"
            );
            this.guildOnly = false;
        }

        @Override
        public void execute(SlashCommandEvent event) {
            LanguageService lang = bot.getSettingsManager().getLanguageService(event.getGuild());
            try {
                String json = configService.exportAllServerConfigsJson();
                String filename = "all_servers_config_" + System.currentTimeMillis() + ".json";
                event.replyFiles(FileUpload.fromData(json.getBytes(StandardCharsets.UTF_8), filename)).queue();
            } catch (Exception e) {
                log.error("Error al exportar todas las configuraciones", e);
                event.reply(lang.getErrorMessage("command.allserverconfig.export.error", e.getMessage())).setEphemeral(true).queue();
            }
        }

        @Override
        public void execute(CommandEvent event) {
            LanguageService lang = bot.getSettingsManager().getLanguageService(event.getGuild());
            try {
                String json = configService.exportAllServerConfigsJson();
                String filename = "all_servers_config_" + System.currentTimeMillis() + ".json";
                event.getChannel().sendFiles(FileUpload.fromData(json.getBytes(StandardCharsets.UTF_8), filename)).queue();
            } catch (Exception e) {
                log.error("Error al exportar todas las configuraciones", e);
                event.replyError(lang.getMessage("command.allserverconfig.export.error", e.getMessage()));
            }
        }
    }

    public static class ImportSubCmd extends OwnerCommand {
        private final ServerConfigService configService;
        private final Bot bot;

        public ImportSubCmd(ServerConfigService configService, Bot bot) {
            super();
            this.configService = configService;
            this.bot = bot;
            this.name = "import";
            this.nameLocalization = Map.of(
                    DiscordLocale.ENGLISH_UK, "import",
                    DiscordLocale.ENGLISH_US, "import",
                    DiscordLocale.SPANISH, "importar",
                    DiscordLocale.SPANISH_LATAM, "importar"
            );
            this.help = "Importación directa de la configuración global de todos los servidores desde JSON.";
            this.descriptionLocalization = Map.of(
                    DiscordLocale.ENGLISH_UK, "Direct import of global configuration for all servers from JSON",
                    DiscordLocale.ENGLISH_US, "Direct import of global configuration for all servers from JSON",
                    DiscordLocale.SPANISH, "Importación directa de la configuración global de todos los servidores desde JSON",
                    DiscordLocale.SPANISH_LATAM, "Importación directa de la configuración global de todos los servidores desde JSON"
            );
            this.guildOnly = false;

            OptionData fileOption = new OptionData(OptionType.ATTACHMENT, "file", "Archivo JSON de configuración global")
                    .setNameLocalizations(Map.of(
                            DiscordLocale.ENGLISH_UK, "file",
                            DiscordLocale.ENGLISH_US, "file",
                            DiscordLocale.SPANISH, "archivo",
                            DiscordLocale.SPANISH_LATAM, "archivo"
                    ))
                    .setDescriptionLocalizations(Map.of(
                            DiscordLocale.ENGLISH_UK, "Global JSON configuration file",
                            DiscordLocale.ENGLISH_US, "Global JSON configuration file",
                            DiscordLocale.SPANISH, "Archivo JSON de configuración global",
                            DiscordLocale.SPANISH_LATAM, "Archivo JSON de configuración global"
                    ))
                    .setRequired(true);

            this.options = Collections.singletonList(fileOption);
        }

        @Override
        public void execute(SlashCommandEvent event) {
            LanguageService lang = bot.getSettingsManager().getLanguageService(event.getGuild());
            Attachment attachment = event.optAttachment("file");
            if (attachment == null) {
                event.reply(lang.getErrorMessage("command.allserverconfig.import.file.required")).setEphemeral(true).queue();
                return;
            }

            try (InputStream is = URI.create(attachment.getUrl()).toURL().openStream()) {
                String jsonContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                List<ServerConfigDTO> dtoList = configService.parseAllServerConfigsJson(jsonContent);

                int count = configService.importAllServerConfigs(dtoList);
                event.reply(lang.getSuccessMessage("command.allserverconfig.import.success", count)).queue();
            } catch (Exception e) {
                log.error("Error al importar todas las configuraciones", e);
                event.reply(lang.getErrorMessage("command.allserverconfig.import.error", e.getMessage())).setEphemeral(true).queue();
            }
        }

        @Override
        public void execute(CommandEvent event) {
            LanguageService lang = bot.getSettingsManager().getLanguageService(event.getGuild());
            List<Attachment> attachments = event.getMessage().getAttachments();
            if (attachments.isEmpty()) {
                event.replyError(lang.getMessage("command.allserverconfig.import.file.required"));
                return;
            }

            Attachment attachment = attachments.get(0);
            try (InputStream is = URI.create(attachment.getUrl()).toURL().openStream()) {
                String jsonContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                List<ServerConfigDTO> dtoList = configService.parseAllServerConfigsJson(jsonContent);

                int count = configService.importAllServerConfigs(dtoList);
                event.replySuccess(lang.getMessage("command.allserverconfig.import.success", count));
            } catch (Exception e) {
                log.error("Error al importar todas las configuraciones", e);
                event.replyError(lang.getMessage("command.allserverconfig.import.error", e.getMessage()));
            }
        }
    }
}
