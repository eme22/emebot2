package com.eme22.bolo.commands.owner;

import com.eme22.bolo.ai.AIChatService;
import com.eme22.bolo.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import java.time.Instant;
import java.util.List;

@Singleton
@Transactional
@ActivateRequestContext
public class AIChatOwnerCmd extends OwnerCommand {

    private final AIChatService aiChatService;

    @Inject
    public AIChatOwnerCmd(AIChatService aiChatService) {
        this.aiChatService = aiChatService;
        this.name = "aiowner";
        this.help = "Configura los parámetros globales del sistema de IA";
        this.children = new OwnerCommand[] {
            new SetupCmd(aiChatService),
            new PromptCmd(aiChatService)
        };
    }

    @Override
    public void execute(SlashCommandEvent event) {
        showConfig(event);
    }

    @Override
    public void execute(CommandEvent event) {
        showConfig(event);
    }

    private void showConfig(SlashCommandEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(0x00A2E8);
        builder.setTitle("🔧 Configuración Global de IA");
        builder.setTimestamp(Instant.now());

        String apiKey = aiChatService.getGlobalApiKey();
        String baseUrl = aiChatService.getGlobalBaseUrl();
        String model = aiChatService.getGlobalModel();
        int timeout = aiChatService.getGlobalTimeoutSeconds();

        builder.addField("API Key", maskApiKey(apiKey), false);
        builder.addField("URL Base", baseUrl != null ? "`" + baseUrl + "`" : "No configurada", true);
        builder.addField("Modelo", model != null ? "`" + model + "`" : "No configurado", true);
        builder.addField("Timeout", "`" + timeout + "s`", true);

        List<AIChatService.AIConfig> backups = aiChatService.getBackupConfigs(baseUrl, model, timeout);
        if (!backups.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (AIChatService.AIConfig b : backups) {
                sb.append("🔒 **Respaldo ").append(b.getIndex()).append(":** ")
                        .append("🔑 `").append(maskApiKey(b.getApiKey())).append("`")
                        .append(" | 🌐 `").append(b.getBaseUrl()).append("`")
                        .append(" | 🤖 `").append(b.getModel()).append("`\n");
            }
            builder.addField("Respaldos Globales", sb.toString(), false);
        }

        builder.setFooter("Usa /aiowner setup para configurar");
        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }

    private void showConfig(CommandEvent event) {
        String apiKey = aiChatService.getGlobalApiKey();
        String baseUrl = aiChatService.getGlobalBaseUrl();
        String model = aiChatService.getGlobalModel();
        int timeout = aiChatService.getGlobalTimeoutSeconds();

        StringBuilder message = new StringBuilder();
        message.append("**Configuración Global de IA**\n");
        message.append("🔑 **API Key:** `").append(maskApiKey(apiKey)).append("`\n");
        message.append("🌐 **URL Base:** `").append(baseUrl != null ? baseUrl : "No configurada").append("`\n");
        message.append("🤖 **Modelo:** `").append(model != null ? model : "No configurado").append("`\n");
        message.append("⏱️ **Timeout:** `").append(timeout).append("s`\n");

        List<AIChatService.AIConfig> backups = aiChatService.getBackupConfigs(baseUrl, model, timeout);
        if (!backups.isEmpty()) {
            message.append("\n**Respaldos Globales:**\n");
            for (AIChatService.AIConfig b : backups) {
                message.append("🔒 **Respaldo ").append(b.getIndex()).append(":** ")
                        .append("🔑 `").append(maskApiKey(b.getApiKey())).append("`")
                        .append(" | 🌐 `").append(b.getBaseUrl()).append("`")
                        .append(" | 🤖 `").append(b.getModel()).append("`\n");
            }
        }

        message.append("\n*Usa `").append(event.getClient().getPrefix()).append("aiowner setup` para configurar*");
        event.reply(message.toString());
    }

    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) return "No configurada";
        if (apiKey.length() <= 10) return "********";
        return apiKey.substring(0, 7) + "..." + apiKey.substring(apiKey.length() - 4);
    }

    private class SetupCmd extends OwnerCommand {
        public SetupCmd(AIChatService aiChatService) {
            this.aiChatService = aiChatService;
            this.name = "setup";
            this.help = "Configura la API de IA global (clave, URL, modelo) o una configuración de respaldo por índice";
            this.options = List.of(
                new OptionData(OptionType.STRING, "api-key", "API Key de OpenAI/DeepSeek").setRequired(false),
                new OptionData(OptionType.STRING, "base-url", "Base URL personalizada").setRequired(false),
                new OptionData(OptionType.STRING, "modelo-ia", "Modelo de IA personalizado").setRequired(false),
                new OptionData(OptionType.INTEGER, "timeout", "Timeout en segundos").setRequired(false),
                new OptionData(OptionType.INTEGER, "indice", "Índice de configuración de respaldo (1-10). Si se omite, se configura la clave principal.").setRequired(false)
            );
        }

        private final AIChatService aiChatService;

        @Override
        public void execute(SlashCommandEvent event) {
            OptionMapping keyOpt = event.getOption("api-key");
            OptionMapping urlOpt = event.getOption("base-url");
            OptionMapping modelOpt = event.getOption("modelo-ia");
            OptionMapping timeoutOpt = event.getOption("timeout");
            OptionMapping indexOpt = event.getOption("indice");

            Integer index = indexOpt != null ? (int) indexOpt.getAsLong() : null;

            if (index != null && (index < 1 || index > 10)) {
                event.reply("❌ El índice debe estar entre 1 y 10.").setEphemeral(true).queue();
                return;
            }

            String apiKey = keyOpt != null ? keyOpt.getAsString() : null;
            String baseUrl = urlOpt != null ? urlOpt.getAsString() : null;
            String model = modelOpt != null ? modelOpt.getAsString() : null;
            Integer timeout = timeoutOpt != null ? (int) timeoutOpt.getAsLong() : null;

            if (index != null) {
                aiChatService.saveBackupConfig(index, apiKey, baseUrl, model, timeout);
                if (apiKey != null && ("none".equalsIgnoreCase(apiKey) || "clear".equalsIgnoreCase(apiKey))) {
                    event.reply("✅ **Configuración de respaldo global " + index + " eliminada con éxito.**").setEphemeral(true).queue();
                } else {
                    event.reply("🔒 **Configuración de respaldo global " + index + " guardada de forma segura.**").setEphemeral(true).queue();
                }
            } else {
                String newApiKey = apiKey != null ? apiKey : aiChatService.getGlobalApiKey();
                String newUrl = baseUrl != null ? baseUrl : aiChatService.getGlobalBaseUrl();
                String newModel = model != null ? model : aiChatService.getGlobalModel();
                int newTimeout = timeout != null ? timeout : aiChatService.getGlobalTimeoutSeconds();
                aiChatService.saveConfig(newApiKey, newUrl, newModel, newTimeout);
                event.reply("🔒 **Configuración global de IA actualizada con éxito.**\n" +
                        "🔑 **API Key:** `" + maskApiKey(newApiKey) + "`\n" +
                        "🌐 **URL Base:** `" + newUrl + "`\n" +
                        "🤖 **Modelo:** `" + newModel + "`\n" +
                        "⏱️ **Timeout:** `" + newTimeout + " segundos`").setEphemeral(true).queue();
            }
        }

        @Override
        public void execute(CommandEvent event) {
            String args = event.getArgs().trim();
            if (args.isEmpty()) {
                event.replyWarning("Uso: `aiowner setup [api-key=...] [base-url=...] [modelo-ia=...] [timeout=...] [indice=...]`");
                return;
            }

            String apiKey = extractValue(args, "api-key", "apikey");
            String baseUrl = extractValue(args, "base-url", "baseurl");
            String model = extractValue(args, "modelo-ia", "modelo", "model");
            String timeoutStr = extractValue(args, "timeout");
            String indexStr = extractValue(args, "indice", "index");

            Integer index = null;
            if (indexStr != null) {
                try {
                    index = Integer.parseInt(indexStr);
                    if (index < 1 || index > 10) {
                        event.replyError("El índice debe estar entre 1 y 10.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    event.replyError("El índice debe ser un número entero.");
                    return;
                }
            }

            Integer timeout = null;
            if (timeoutStr != null) {
                try {
                    timeout = Integer.parseInt(timeoutStr);
                } catch (NumberFormatException e) {
                    event.replyError("El timeout debe ser un número entero.");
                    return;
                }
            }

            if (index != null) {
                aiChatService.saveBackupConfig(index, apiKey, baseUrl, model, timeout);
                if (apiKey != null && ("none".equalsIgnoreCase(apiKey) || "clear".equalsIgnoreCase(apiKey))) {
                    event.replySuccess("Configuración de respaldo global " + index + " eliminada con éxito.");
                } else {
                    event.replySuccess("Configuración de respaldo global " + index + " guardada de forma segura.");
                }
            } else {
                String newApiKey = apiKey != null ? apiKey : aiChatService.getGlobalApiKey();
                String newUrl = baseUrl != null ? baseUrl : aiChatService.getGlobalBaseUrl();
                String newModel = model != null ? model : aiChatService.getGlobalModel();
                int newTimeout = timeout != null ? timeout : aiChatService.getGlobalTimeoutSeconds();
                aiChatService.saveConfig(newApiKey, newUrl, newModel, newTimeout);
                event.replySuccess("Configuración global de IA actualizada.\n" +
                        "API Key: " + maskApiKey(newApiKey) + "\n" +
                        "URL: " + newUrl + "\n" +
                        "Modelo: " + newModel + "\n" +
                        "Timeout: " + newTimeout + "s");
            }
        }

        private String extractValue(String args, String... keys) {
            for (String key : keys) {
                String prefix = key + "=";
                int idx = args.indexOf(prefix);
                if (idx != -1) {
                    int start = idx + prefix.length();
                    int end = args.length();
                    String[] allKeys = {"api-key=", "apikey=", "base-url=", "baseurl=", "modelo-ia=", "modelo=", "model=", "timeout=", "indice=", "index="};
                    for (String otherKey : allKeys) {
                        int nextIdx = args.indexOf(otherKey, start);
                        if (nextIdx != -1 && nextIdx < end) {
                            end = nextIdx;
                        }
                    }
                    String value = args.substring(start, end).trim();
                    if (!value.isEmpty()) {
                        return value;
                    }
                }
            }
            return null;
        }
    }

    private class PromptCmd extends OwnerCommand {
        private final AIChatService aiChatService;

        public PromptCmd(AIChatService aiChatService) {
            this.aiChatService = aiChatService;
            this.name = "prompt";
            this.help = "Establece o consulta el system prompt global de la IA";
            this.options = List.of(
                new OptionData(OptionType.STRING, "texto", "Texto del system prompt. Usa 'default' para restablecer. Omitir para mostrar el actual.").setRequired(false)
            );
        }

        @Override
        public void execute(SlashCommandEvent event) {
            OptionMapping textOpt = event.getOption("texto");
            if (textOpt == null) {
                String currentPrompt = aiChatService.getGlobalSystemPrompt();
                if (currentPrompt == null || currentPrompt.isEmpty()) {
                    event.reply("📝 **System Prompt actual:** Usando el valor por defecto del sistema.").setEphemeral(true).queue();
                } else {
                    String preview = currentPrompt.length() > 1000 ? currentPrompt.substring(0, 997) + "..." : currentPrompt;
                    event.reply("📝 **System Prompt actual:**\n```\n" + preview + "\n```").setEphemeral(true).queue();
                }
                return;
            }

            String text = textOpt.getAsString();
            if ("default".equalsIgnoreCase(text) || "clear".equalsIgnoreCase(text) || "none".equalsIgnoreCase(text)) {
                aiChatService.saveSystemPrompt(null);
                event.reply("🔄 **System Prompt restablecido al valor por defecto del sistema.**").setEphemeral(true).queue();
            } else {
                aiChatService.saveSystemPrompt(text);
                event.reply("✅ **System Prompt actualizado correctamente.**").setEphemeral(true).queue();
            }
        }

        @Override
        public void execute(CommandEvent event) {
            String args = event.getArgs().trim();
            if (args.isEmpty()) {
                String currentPrompt = aiChatService.getGlobalSystemPrompt();
                if (currentPrompt == null || currentPrompt.isEmpty()) {
                    event.reply("📝 **System Prompt actual:** Usando el valor por defecto del sistema.");
                } else {
                    String preview = currentPrompt.length() > 1000 ? currentPrompt.substring(0, 997) + "..." : currentPrompt;
                    event.reply("📝 **System Prompt actual:**\n```\n" + preview + "\n```");
                }
                return;
            }

            if ("default".equalsIgnoreCase(args) || "clear".equalsIgnoreCase(args) || "none".equalsIgnoreCase(args)) {
                aiChatService.saveSystemPrompt(null);
                event.replySuccess("System Prompt restablecido al valor por defecto del sistema.");
            } else {
                aiChatService.saveSystemPrompt(args);
                event.replySuccess("System Prompt actualizado correctamente.");
            }
        }
    }
}
