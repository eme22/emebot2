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
        this.help = "Configura dinámicamente los parámetros globales del modelo de IA";
        this.arguments = "[api-key=xxx] [url=xxx] [model=xxx] [timeout=xxx]";
        this.guildOnly = false;
        this.options = Arrays.asList(
            new OptionData(OptionType.STRING, "api-key", "API Key de OpenAI/OpenRouter").setRequired(false),
            new OptionData(OptionType.STRING, "url", "Base URL de la API de IA").setRequired(false),
            new OptionData(OptionType.STRING, "model", "Nombre del modelo de IA").setRequired(false),
            new OptionData(OptionType.INTEGER, "timeout", "Timeout en segundos").setRequired(false)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String apiKey = event.optString("api-key", null);
        String url = event.optString("url", null);
        String model = event.optString("model", null);
        OptionMapping timeoutOpt = event.getOption("timeout");
        Integer timeout = timeoutOpt != null ? (int) timeoutOpt.getAsLong() : null;

        if (apiKey == null && url == null && model == null && timeout == null) {
            showSettings(event);
        } else {
            updateSettings(event, apiKey, url, model, timeout);
        }
    }

    @Override
    public void execute(CommandEvent event) {
        String args = event.getArgs().trim();
        if (args.isEmpty()) {
            showSettings(event);
            return;
        }

        String apiKey = null;
        String url = null;
        String model = null;
        Integer timeout = null;

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
            for (String part : parts) {
                if (part.contains("=")) {
                    String[] kv = part.split("=", 2);
                    String key = kv[0].toLowerCase();
                    String val = kv[1];
                    if (key.equals("apikey") || key.equals("api-key")) {
                        apiKey = val;
                    } else if (key.equals("url")) {
                        url = val;
                    } else if (key.equals("model")) {
                        model = val;
                    } else if (key.equals("timeout")) {
                        try {
                            timeout = Integer.parseInt(val);
                        } catch (NumberFormatException e) {
                            event.replyError("El timeout debe ser un número entero.");
                            return;
                        }
                    }
                } else {
                    event.replyError("Formato incorrecto. Usa `key=value` (ej: `apikey=xxx url=xxx`) o 4 argumentos posicionales.");
                    return;
                }
            }
        }

        updateSettings(event, apiKey, url, model, timeout);
    }

    private void showSettings(SlashCommandEvent event) {
        String activeApiKey = aiChatService.getGlobalApiKey();
        String activeUrl = aiChatService.getGlobalBaseUrl();
        String activeModel = aiChatService.getGlobalModel();
        int activeTimeout = aiChatService.getGlobalTimeoutSeconds();

        String message = "**Configuración Global de IA Actual:**\n" +
                "🔑 **API Key:** `" + maskApiKey(activeApiKey) + "`\n" +
                "🌐 **URL Base:** `" + (activeUrl != null ? activeUrl : "No configurada") + "`\n" +
                "🤖 **Modelo:** `" + (activeModel != null ? activeModel : "No configurado") + "`\n" +
                "⏱️ **Timeout:** `" + activeTimeout + " segundos`\n\n" +
                "💡 *Puedes actualizarla usando `/setai [api-key] [url] [model] [timeout]` o `!setai apikey=xxx ...`*";
        event.reply(message).setEphemeral(true).queue();
    }

    private void showSettings(CommandEvent event) {
        String activeApiKey = aiChatService.getGlobalApiKey();
        String activeUrl = aiChatService.getGlobalBaseUrl();
        String activeModel = aiChatService.getGlobalModel();
        int activeTimeout = aiChatService.getGlobalTimeoutSeconds();

        String message = "**Configuración Global de IA Actual:**\n" +
                "🔑 **API Key:** `" + maskApiKey(activeApiKey) + "`\n" +
                "🌐 **URL Base:** `" + (activeUrl != null ? activeUrl : "No configurada") + "`\n" +
                "🤖 **Modelo:** `" + (activeModel != null ? activeModel : "No configurado") + "`\n" +
                "⏱️ **Timeout:** `" + activeTimeout + " segundos`\n\n" +
                "💡 *Puedes actualizarla usando `!setai apikey=xxx url=xxx ...` o posicionalmente: `!setai <apikey> <url> <model> <timeout>`*";
        event.reply(message);
    }

    private void updateSettings(SlashCommandEvent event, String apiKey, String url, String model, Integer timeout) {
        String newApiKey = apiKey != null ? apiKey : aiChatService.getGlobalApiKey();
        String newUrl = url != null ? url : aiChatService.getGlobalBaseUrl();
        String newModel = model != null ? model : aiChatService.getGlobalModel();
        int newTimeout = timeout != null ? timeout : aiChatService.getGlobalTimeoutSeconds();

        aiChatService.saveConfig(newApiKey, newUrl, newModel, newTimeout);

        event.reply(event.getClient().getSuccess() + " **Configuración de IA actualizada con éxito:**\n" +
                "🔑 **API Key:** `" + maskApiKey(newApiKey) + "`\n" +
                "🌐 **URL Base:** `" + newUrl + "`\n" +
                "🤖 **Modelo:** `" + newModel + "`\n" +
                "⏱️ **Timeout:** `" + newTimeout + " segundos`").setEphemeral(true).queue();
    }

    private void updateSettings(CommandEvent event, String apiKey, String url, String model, Integer timeout) {
        String newApiKey = apiKey != null ? apiKey : aiChatService.getGlobalApiKey();
        String newUrl = url != null ? url : aiChatService.getGlobalBaseUrl();
        String newModel = model != null ? model : aiChatService.getGlobalModel();
        int newTimeout = timeout != null ? timeout : aiChatService.getGlobalTimeoutSeconds();

        aiChatService.saveConfig(newApiKey, newUrl, newModel, newTimeout);

        event.reply(event.getClient().getSuccess() + " **Configuración de IA actualizada con éxito:**\n" +
                "🔑 **API Key:** `" + maskApiKey(newApiKey) + "`\n" +
                "🌐 **URL Base:** `" + newUrl + "`\n" +
                "🤖 **Modelo:** `" + newModel + "`\n" +
                "⏱️ **Timeout:** `" + newTimeout + " segundos`");
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
}
