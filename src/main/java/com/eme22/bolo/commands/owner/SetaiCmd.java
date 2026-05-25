package com.eme22.bolo.commands.owner;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import com.eme22.bolo.commands.OwnerCommand;
import com.eme22.bolo.ai.AIChatService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import java.util.Arrays;

@Singleton
@Transactional
@ActivateRequestContext
public class SetaiCmd extends OwnerCommand {

    @Inject
    AIChatService aiChatService;

    public SetaiCmd() {
        this.name = "setai";
        this.help = "Configura dinámicamente los parámetros globales del modelo de IA y prompts del sistema";
        this.arguments = "[api-key=xxx] [url=xxx] [model=xxx] [timeout=xxx] [prompt-normal=xxx] [prompt-admin=xxx]";
        this.guildOnly = false;
        this.options = Arrays.asList(
            new OptionData(OptionType.STRING, "api-key", "API Key de OpenAI/OpenRouter").setRequired(false),
            new OptionData(OptionType.STRING, "url", "Base URL de la API de IA").setRequired(false),
            new OptionData(OptionType.STRING, "model", "Nombre del modelo de IA").setRequired(false),
            new OptionData(OptionType.INTEGER, "timeout", "Timeout en segundos").setRequired(false),
            new OptionData(OptionType.STRING, "prompt-normal", "System prompt para modo normal").setRequired(false),
            new OptionData(OptionType.STRING, "prompt-admin", "System prompt para modo administración").setRequired(false)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String apiKey = event.optString("api-key", null);
        String url = event.optString("url", null);
        String model = event.optString("model", null);
        OptionMapping timeoutOpt = event.getOption("timeout");
        Integer timeout = timeoutOpt != null ? (int) timeoutOpt.getAsLong() : null;
        String promptNormal = event.optString("prompt-normal", null);
        String promptAdmin = event.optString("prompt-admin", null);

        if (apiKey == null && url == null && model == null && timeout == null && promptNormal == null && promptAdmin == null) {
            showSettings(event);
        } else {
            updateSettings(event, apiKey, url, model, timeout, promptNormal, promptAdmin);
        }
    }

    @Override
    public void execute(CommandEvent event) {
        String args = event.getArgs().trim();
        if (args.isEmpty()) {
            showSettings(event);
            return;
        }

        parseArgsAndSet(args, event);
    }

    private void parseArgsAndSet(String args, CommandEvent event) {
        String apiKey = extractValue(args, new String[]{"apikey", "api-key"});
        String url = extractValue(args, new String[]{"url"});
        String model = extractValue(args, new String[]{"model"});
        String timeoutStr = extractValue(args, new String[]{"timeout"});
        String promptNormal = extractValue(args, new String[]{"prompt-normal"});
        String promptAdmin = extractValue(args, new String[]{"prompt-admin"});

        Integer timeout = null;
        if (timeoutStr != null) {
            try {
                timeout = Integer.parseInt(timeoutStr);
            } catch (NumberFormatException e) {
                event.replyError("El timeout debe ser un número entero.");
                return;
            }
        }

        if (apiKey == null && url == null && model == null && timeout == null && promptNormal == null && promptAdmin == null) {
            String[] parts = args.split("\\s+");
            if (parts.length == 4 && !args.contains("=")) {
                apiKey = parts[0];
                url = parts[1];
                model = parts[2];
                try {
                    timeout = Integer.parseInt(parts[3]);
                } catch (NumberFormatException e) {
                    event.replyError("El timeout debe ser un número entero.");
                    return;
                }
            } else {
                event.replyError("Formato incorrecto. Usa `key=value` (ej: `apikey=xxx url=xxx`) o 4 argumentos posicionales.");
                return;
            }
        }

        updateSettings(event, apiKey, url, model, timeout, promptNormal, promptAdmin);
    }

    private String extractValue(String args, String[] keys) {
        for (String key : keys) {
            String prefix = key + "=";
            int idx = args.indexOf(prefix);
            if (idx != -1) {
                int start = idx + prefix.length();
                int end = args.length();
                String[] allKeys = {"apikey=", "api-key=", "url=", "model=", "timeout=", "prompt-normal=", "prompt-admin="};
                for (String otherKey : allKeys) {
                    int nextIdx = args.indexOf(otherKey, start);
                    if (nextIdx != -1 && nextIdx < end) {
                        end = nextIdx;
                    }
                }
                return args.substring(start, end).trim();
            }
        }
        return null;
    }

    private void showSettings(SlashCommandEvent event) {
        String activeApiKey = aiChatService.getGlobalApiKey();
        String activeUrl = aiChatService.getGlobalBaseUrl();
        String activeModel = aiChatService.getGlobalModel();
        int activeTimeout = aiChatService.getGlobalTimeoutSeconds();
        String promptNormal = aiChatService.getGlobalSystemPrompt("normal");
        String promptAdmin = aiChatService.getGlobalSystemPrompt("admin");

        String message = "**Configuración Global de IA Actual:**\n" +
                "🔑 **API Key:** `" + maskApiKey(activeApiKey) + "`\n" +
                "🌐 **URL Base:** `" + (activeUrl != null ? activeUrl : "No configurada") + "`\n" +
                "🤖 **Modelo:** `" + (activeModel != null ? activeModel : "No configurado") + "`\n" +
                "⏱️ **Timeout:** `" + activeTimeout + " segundos`\n" +
                "📝 **System Prompt (Normal):** " + formatPromptPreview(promptNormal) + "\n" +
                "🛡️ **System Prompt (Admin):** " + formatPromptPreview(promptAdmin) + "\n\n" +
                "💡 *Puedes actualizarla usando `/setai [api-key] [url] [model] [timeout] [prompt-normal] [prompt-admin]` o `!setai apikey=xxx ...`*";
        event.reply(message).setEphemeral(true).queue();
    }

    private void showSettings(CommandEvent event) {
        String activeApiKey = aiChatService.getGlobalApiKey();
        String activeUrl = aiChatService.getGlobalBaseUrl();
        String activeModel = aiChatService.getGlobalModel();
        int activeTimeout = aiChatService.getGlobalTimeoutSeconds();
        String promptNormal = aiChatService.getGlobalSystemPrompt("normal");
        String promptAdmin = aiChatService.getGlobalSystemPrompt("admin");

        String message = "**Configuración Global de IA Actual:**\n" +
                "🔑 **API Key:** `" + maskApiKey(activeApiKey) + "`\n" +
                "🌐 **URL Base:** `" + (activeUrl != null ? activeUrl : "No configurada") + "`\n" +
                "🤖 **Modelo:** `" + (activeModel != null ? activeModel : "No configurado") + "`\n" +
                "⏱️ **Timeout:** `" + activeTimeout + " segundos`\n" +
                "📝 **System Prompt (Normal):** " + formatPromptPreview(promptNormal) + "\n" +
                "🛡️ **System Prompt (Admin):** " + formatPromptPreview(promptAdmin) + "\n\n" +
                "💡 *Puedes actualizarla usando `!setai apikey=xxx url=xxx ...` o posicionalmente: `!setai <apikey> <url> <model> <timeout>`*";
        event.reply(message);
    }

    private void updateSettings(SlashCommandEvent event, String apiKey, String url, String model, Integer timeout, String promptNormal, String promptAdmin) {
        String newApiKey = apiKey != null ? apiKey : aiChatService.getGlobalApiKey();
        String newUrl = url != null ? url : aiChatService.getGlobalBaseUrl();
        String newModel = model != null ? model : aiChatService.getGlobalModel();
        int newTimeout = timeout != null ? timeout : aiChatService.getGlobalTimeoutSeconds();

        aiChatService.saveConfig(newApiKey, newUrl, newModel, newTimeout);

        if (promptNormal != null) {
            aiChatService.saveSystemPrompt("normal", promptNormal);
        }
        if (promptAdmin != null) {
            aiChatService.saveSystemPrompt("admin", promptAdmin);
        }

        String activePromptNormal = aiChatService.getGlobalSystemPrompt("normal");
        String activePromptAdmin = aiChatService.getGlobalSystemPrompt("admin");

        event.reply(event.getClient().getSuccess() + " **Configuración de IA actualizada con éxito:**\n" +
                "🔑 **API Key:** `" + maskApiKey(newApiKey) + "`\n" +
                "🌐 **URL Base:** `" + newUrl + "`\n" +
                "🤖 **Modelo:** `" + newModel + "`\n" +
                "⏱️ **Timeout:** `" + newTimeout + " segundos`\n" +
                "📝 **System Prompt (Normal):** " + formatPromptPreview(activePromptNormal) + "\n" +
                "🛡️ **System Prompt (Admin):** " + formatPromptPreview(activePromptAdmin)).setEphemeral(true).queue();
    }

    private void updateSettings(CommandEvent event, String apiKey, String url, String model, Integer timeout, String promptNormal, String promptAdmin) {
        String newApiKey = apiKey != null ? apiKey : aiChatService.getGlobalApiKey();
        String newUrl = url != null ? url : aiChatService.getGlobalBaseUrl();
        String newModel = model != null ? model : aiChatService.getGlobalModel();
        int newTimeout = timeout != null ? timeout : aiChatService.getGlobalTimeoutSeconds();

        aiChatService.saveConfig(newApiKey, newUrl, newModel, newTimeout);

        if (promptNormal != null) {
            aiChatService.saveSystemPrompt("normal", promptNormal);
        }
        if (promptAdmin != null) {
            aiChatService.saveSystemPrompt("admin", promptAdmin);
        }

        String activePromptNormal = aiChatService.getGlobalSystemPrompt("normal");
        String activePromptAdmin = aiChatService.getGlobalSystemPrompt("admin");

        event.reply(event.getClient().getSuccess() + " **Configuración de IA actualizada con éxito:**\n" +
                "🔑 **API Key:** `" + maskApiKey(newApiKey) + "`\n" +
                "🌐 **URL Base:** `" + newUrl + "`\n" +
                "🤖 **Modelo:** `" + newModel + "`\n" +
                "⏱️ **Timeout:** `" + newTimeout + " segundos`\n" +
                "📝 **System Prompt (Normal):** " + formatPromptPreview(activePromptNormal) + "\n" +
                "🛡️ **System Prompt (Admin):** " + formatPromptPreview(activePromptAdmin));
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty() || "none".equalsIgnoreCase(apiKey)) {
            return "No configurada";
        }
        if (apiKey.length() <= 10) {
            return "********";
        }
        return apiKey.substring(0, 7) + "..." + apiKey.substring(apiKey.length() - 4);
    }

    private String formatPromptPreview(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            return "*Por defecto (sistema)*";
        }
        if (prompt.length() <= 60) {
            return "`" + prompt + "`";
        }
        return "`" + prompt.substring(0, 57) + "...`";
    }
}
