package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.dto.ServerConfigDTO;
import com.eme22.bolo.dto.ValidationReport;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.services.ServerConfigService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
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
public class ServerConfigCmd extends AdminCommand {

    @ConfigProperty(name = "config.aliases.serverconfig", defaultValue = "")
    protected String[] aliases;

    private final Bot bot;

    @Inject
    public ServerConfigCmd(@Named("adminCategory") Category category, Bot bot, ServerConfigService configService) {
        super(category);
        this.bot = bot;
        this.name = "serverconfig";
        this.nameLocalization = Map.of(
                DiscordLocale.ENGLISH_UK, "serverconfig",
                DiscordLocale.ENGLISH_US, "serverconfig",
                DiscordLocale.SPANISH, "configservidor",
                DiscordLocale.SPANISH_LATAM, "configservidor"
        );
        this.help = "Importa o exporta la configuración del servidor en formato JSON.";
        this.descriptionLocalization = Map.of(
                DiscordLocale.ENGLISH_UK, "Import or export server configuration in JSON format",
                DiscordLocale.ENGLISH_US, "Import or export server configuration in JSON format",
                DiscordLocale.SPANISH, "Importa o exporta la configuración del servidor en formato JSON",
                DiscordLocale.SPANISH_LATAM, "Importa o exporta la configuración del servidor en formato JSON"
        );
        this.guildOnly = true;
        this.children = new SlashCommand[]{
                new ExportSubCmd(configService, bot),
                new ImportSubCmd(configService, bot)
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        LanguageService lang = bot.getSettingsManager().getLanguageService(event.getGuild());
        event.reply(lang.getMessage("command.serverconfig.subcommand.required")).setEphemeral(true).queue();
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
            event.replyError(lang.getMessage("command.serverconfig.usage", event.getClient().getPrefix()));
        }
    }

    public static class ExportSubCmd extends AdminCommand {
        private final ServerConfigService configService;
        private final Bot bot;

        public ExportSubCmd(ServerConfigService configService, Bot bot) {
            super(null);
            this.configService = configService;
            this.bot = bot;
            this.name = "export";
            this.nameLocalization = Map.of(
                    DiscordLocale.ENGLISH_UK, "export",
                    DiscordLocale.ENGLISH_US, "export",
                    DiscordLocale.SPANISH, "exportar",
                    DiscordLocale.SPANISH_LATAM, "exportar"
            );
            this.help = "Exporta la configuración del servidor a un archivo JSON.";
            this.descriptionLocalization = Map.of(
                    DiscordLocale.ENGLISH_UK, "Export server configuration to a JSON file",
                    DiscordLocale.ENGLISH_US, "Export server configuration to a JSON file",
                    DiscordLocale.SPANISH, "Exporta la configuración del servidor a un archivo JSON",
                    DiscordLocale.SPANISH_LATAM, "Exporta la configuración del servidor a un archivo JSON"
            );
            this.guildOnly = true;
        }

        @Override
        public void execute(SlashCommandEvent event) {
            LanguageService lang = bot.getSettingsManager().getLanguageService(event.getGuild());
            try {
                Server server = bot.getSettingsManager().getSettings(event.getGuild());
                String json = configService.exportServerConfigJson(server);
                String filename = "server_config_" + event.getGuild().getId() + ".json";
                event.replyFiles(FileUpload.fromData(json.getBytes(StandardCharsets.UTF_8), filename)).queue();
            } catch (Exception e) {
                log.error("Error al exportar configuración del servidor", e);
                event.reply(lang.getErrorMessage("command.serverconfig.export.error", e.getMessage())).setEphemeral(true).queue();
            }
        }

        @Override
        public void execute(CommandEvent event) {
            LanguageService lang = bot.getSettingsManager().getLanguageService(event.getGuild());
            try {
                Server server = bot.getSettingsManager().getSettings(event.getGuild());
                String json = configService.exportServerConfigJson(server);
                String filename = "server_config_" + event.getGuild().getId() + ".json";
                event.getChannel().sendFiles(FileUpload.fromData(json.getBytes(StandardCharsets.UTF_8), filename)).queue();
            } catch (Exception e) {
                log.error("Error al exportar configuración del servidor", e);
                event.replyError(lang.getMessage("command.serverconfig.export.error", e.getMessage()));
            }
        }
    }

    public static class ImportSubCmd extends AdminCommand {
        private final ServerConfigService configService;
        private final Bot bot;

        public ImportSubCmd(ServerConfigService configService, Bot bot) {
            super(null);
            this.configService = configService;
            this.bot = bot;
            this.name = "import";
            this.nameLocalization = Map.of(
                    DiscordLocale.ENGLISH_UK, "import",
                    DiscordLocale.ENGLISH_US, "import",
                    DiscordLocale.SPANISH, "importar",
                    DiscordLocale.SPANISH_LATAM, "importar"
            );
            this.help = "Importa la configuración del servidor desde un archivo JSON adjunto.";
            this.descriptionLocalization = Map.of(
                    DiscordLocale.ENGLISH_UK, "Import server configuration from an attached JSON file",
                    DiscordLocale.ENGLISH_US, "Import server configuration from an attached JSON file",
                    DiscordLocale.SPANISH, "Importa la configuración del servidor desde un archivo JSON adjunto",
                    DiscordLocale.SPANISH_LATAM, "Importa la configuración del servidor desde un archivo JSON adjunto"
            );
            this.guildOnly = true;

            OptionData fileOption = new OptionData(OptionType.ATTACHMENT, "file", "Archivo JSON de configuración")
                    .setNameLocalizations(Map.of(
                            DiscordLocale.ENGLISH_UK, "file",
                            DiscordLocale.ENGLISH_US, "file",
                            DiscordLocale.SPANISH, "archivo",
                            DiscordLocale.SPANISH_LATAM, "archivo"
                    ))
                    .setDescriptionLocalizations(Map.of(
                            DiscordLocale.ENGLISH_UK, "JSON configuration file",
                            DiscordLocale.ENGLISH_US, "JSON configuration file",
                            DiscordLocale.SPANISH, "Archivo JSON de configuración",
                            DiscordLocale.SPANISH_LATAM, "Archivo JSON de configuración"
                    ))
                    .setRequired(true);

            this.options = Collections.singletonList(fileOption);
        }

        @Override
        public void execute(SlashCommandEvent event) {
            LanguageService lang = bot.getSettingsManager().getLanguageService(event.getGuild());
            Attachment attachment = event.optAttachment("file");
            if (attachment == null) {
                event.reply(lang.getErrorMessage("command.serverconfig.import.file.required")).setEphemeral(true).queue();
                return;
            }

            try (InputStream is = URI.create(attachment.getUrl()).toURL().openStream()) {
                String jsonContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                // Paso 2: Parseo
                ServerConfigDTO dto = configService.parseServerConfigJson(jsonContent);

                // Paso 3: Validación y Reporte
                ValidationReport report = configService.validateServerConfig(event.getGuild(), dto, lang);
                String reportMessage = report.toFormattedReport(lang);

                if (!report.isValid()) {
                    event.reply(lang.getErrorMessage("command.serverconfig.import.validation_failed") + "\n" + reportMessage).setEphemeral(true).queue();
                    return;
                }

                // Paso 4: Aplicación (Merge)
                configService.importServerConfig(event.getGuild().getIdLong(), dto);

                event.reply(reportMessage + "\n" + lang.getSuccessMessage("command.serverconfig.import.success")).queue();
            } catch (Exception e) {
                log.error("Error al importar configuración del servidor", e);
                event.reply(lang.getErrorMessage("command.serverconfig.import.error", e.getMessage())).setEphemeral(true).queue();
            }
        }

        @Override
        public void execute(CommandEvent event) {
            LanguageService lang = bot.getSettingsManager().getLanguageService(event.getGuild());
            List<Attachment> attachments = event.getMessage().getAttachments();
            if (attachments.isEmpty()) {
                event.replyError(lang.getMessage("command.serverconfig.import.file.required"));
                return;
            }

            Attachment attachment = attachments.get(0);
            try (InputStream is = URI.create(attachment.getUrl()).toURL().openStream()) {
                String jsonContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                // Paso 2: Parseo
                ServerConfigDTO dto = configService.parseServerConfigJson(jsonContent);

                // Paso 3: Validación y Reporte
                ValidationReport report = configService.validateServerConfig(event.getGuild(), dto, lang);
                String reportMessage = report.toFormattedReport(lang);

                if (!report.isValid()) {
                    event.replyError(lang.getMessage("command.serverconfig.import.validation_failed") + "\n" + reportMessage);
                    return;
                }

                // Paso 4: Aplicación (Merge)
                configService.importServerConfig(event.getGuild().getIdLong(), dto);

                event.replySuccess(reportMessage + "\n" + lang.getSuccessMessage("command.serverconfig.import.success"));
            } catch (Exception e) {
                log.error("Error al importar configuración del servidor", e);
                event.replyError(lang.getMessage("command.serverconfig.import.error", e.getMessage()));
            }
        }
    }
}
