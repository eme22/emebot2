package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.ai.AIChatService;
import com.eme22.bolo.ai.AIChatSessionManager;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.repository.AIChatMessageRepository;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import java.time.Instant;
import java.util.List;

@Singleton
@Transactional
@ActivateRequestContext
public class AIChatCmd extends AdminCommand {

    private final Bot bot;
    private final AIChatSessionManager sessionManager;
    private final AIChatMessageRepository messageRepository;
    private final com.eme22.bolo.repository.AIChatSessionSummaryRepository sessionSummaryRepository;
    private final AIChatService aiChatService;

    @Inject
    public AIChatCmd(Bot bot, AIChatSessionManager sessionManager, AIChatMessageRepository messageRepository, com.eme22.bolo.repository.AIChatSessionSummaryRepository sessionSummaryRepository, AIChatService aiChatService, @Named("adminCategory") Category category) {
        super(category);
        this.bot = bot;
        this.sessionManager = sessionManager;
        this.messageRepository = messageRepository;
        this.sessionSummaryRepository = sessionSummaryRepository;
        this.aiChatService = aiChatService;
        this.name = "ai";
        this.help = "Configura y administra el chatbot de IA del servidor";
        this.children = new AdminCommand[] {
            new EnableCmd(bot, category),
            new DisableCmd(bot, category),
            new ResetCmd(bot, sessionManager, messageRepository, sessionSummaryRepository, category),
            new StatusCmd(bot, aiChatService, category),
            new ChannelCmd(bot, category),
            new SetupCmd(bot, aiChatService, category),
            new ExclusiveCmd(bot, category)
        };
    }

    @Override
    public void execute(SlashCommandEvent event) {
        Server server = bot.getSettingsManager().getSettings(event.getGuild());
        event.replyEmbeds(buildStatusEmbed(aiChatService, server, event.getGuild().getName())).queue();
    }

    @Override
    public void execute(CommandEvent event) {
        Server server = bot.getSettingsManager().getSettings(event.getGuild());
        event.reply(buildStatusEmbed(aiChatService, server, event.getGuild().getName()));
    }

    private static net.dv8tion.jda.api.entities.MessageEmbed buildStatusEmbed(AIChatService aiChatService, Server server, String guildName) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(0x00A2E8);
        builder.setTitle("🤖 Estado del Chatbot de IA - " + guildName);
        builder.setTimestamp(Instant.now());

        builder.addField("Habilitado", server.isAiEnabled() ? "🟢 Sí" : "🔴 No", true);

        long channelId = server.getAiChannelId();
        String channelStr = channelId == 0L ? "Ninguno (Responder a Menciones `@mention`)" : "<#" + channelId + ">";
        builder.addField("Canal Dedicado", channelStr, false);
        builder.addField("Exclusivo en Canal", server.isAiExclusive() ? "🔒 Sí (Solo responde en el canal dedicado)" : "🔓 No (Responde a menciones en otros canales)", false);

        String model = server.getAiModel() != null ? server.getAiModel() : "Global Default (gpt-4o-mini)";
        String baseUrl = server.getAiBaseUrl() != null ? server.getAiBaseUrl() : "Global Default (OpenAI)";
        builder.addField("Modelo", "`" + model + "`", true);
        builder.addField("Proveedor URL Base", "`" + baseUrl + "`", true);
        builder.addField("Clave API Personalizada", server.getAiApiKey() != null ? "🔒 Configurada" : "❌ No (Usa global)", false);

        List<com.eme22.bolo.ai.AIChatService.AIConfig> serverBackups = aiChatService.getServerBackupConfigs(
                server.getId(), "N/A", "N/A", 0);
        if (!serverBackups.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (com.eme22.bolo.ai.AIChatService.AIConfig b : serverBackups) {
                sb.append("🔒 **Respaldo ").append(b.getIndex()).append(":** ")
                        .append("🔑 `").append(maskApiKey(b.getApiKey())).append("`")
                        .append(" | 🌐 `").append(b.getBaseUrl()).append("`")
                        .append(" | 🤖 `").append(b.getModel()).append("`\n");
            }
            builder.addField("Respaldos del Servidor", sb.toString(), false);
        }

        builder.setFooter("EMBot AI Subsystem");
        return builder.build();
    }

    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) return "No configurada";
        if (apiKey.length() <= 10) return "********";
        return apiKey.substring(0, 7) + "..." + apiKey.substring(apiKey.length() - 4);
    }

    private static class EnableCmd extends AdminCommand {
        private final Bot bot;

        public EnableCmd(Bot bot, Category category) {
            super(category);
            this.bot = bot;
            this.name = "enable";
            this.help = "Habilita el chatbot de IA en el servidor";
        }

        @Override
        public void execute(SlashCommandEvent event) {
            Server server = bot.getSettingsManager().getSettings(event.getGuild());
            server.setAiEnabled(true);
            server.save();
            event.reply("✅ **Chatbot de IA Habilitado**. Ahora puedes chatear conmigo en el servidor.").queue();
        }

        @Override
        public void execute(CommandEvent event) {
            Server server = bot.getSettingsManager().getSettings(event.getGuild());
            server.setAiEnabled(true);
            server.save();
            event.replySuccess("Chatbot de IA Habilitado.");
        }
    }

    private static class DisableCmd extends AdminCommand {
        private final Bot bot;

        public DisableCmd(Bot bot, Category category) {
            super(category);
            this.bot = bot;
            this.name = "disable";
            this.help = "Deshabilita el chatbot de IA en el servidor";
        }

        @Override
        public void execute(SlashCommandEvent event) {
            Server server = bot.getSettingsManager().getSettings(event.getGuild());
            server.setAiEnabled(false);
            server.save();
            event.reply("❌ **Chatbot de IA Deshabilitado**.").queue();
        }

        @Override
        public void execute(CommandEvent event) {
            Server server = bot.getSettingsManager().getSettings(event.getGuild());
            server.setAiEnabled(false);
            server.save();
            event.replySuccess("Chatbot de IA Deshabilitado.");
        }
    }

    private static class ResetCmd extends AdminCommand {
        private final Bot bot;
        private final AIChatSessionManager sessionManager;
        private final AIChatMessageRepository messageRepository;
        private final com.eme22.bolo.repository.AIChatSessionSummaryRepository sessionSummaryRepository;

        public ResetCmd(Bot bot, AIChatSessionManager sessionManager, AIChatMessageRepository messageRepository, com.eme22.bolo.repository.AIChatSessionSummaryRepository sessionSummaryRepository, Category category) {
            super(category);
            this.bot = bot;
            this.sessionManager = sessionManager;
            this.messageRepository = messageRepository;
            this.sessionSummaryRepository = sessionSummaryRepository;
            this.name = "reset";
            this.help = "Reinicia el historial de conversación con la IA en este canal";
        }

        @Override
        public void execute(SlashCommandEvent event) {
            Long guildId = event.getGuild().getIdLong();
            Long channelId = event.getChannel().getIdLong();
            Long userId = event.getUser().getIdLong();
            String activeSession = sessionManager.getOrCreateSession(guildId, channelId, userId);
            sessionManager.forceReset(guildId, channelId, userId);
            messageRepository.deleteSession(activeSession);
            sessionSummaryRepository.deleteBySessionId(activeSession);
            event.reply("🔄 **Historial de conversación reiniciado**. He olvidado nuestro contexto previo en este canal. ¡Empecemos de nuevo!").queue();
        }

        @Override
        public void execute(CommandEvent event) {
            Long guildId = event.getGuild().getIdLong();
            Long channelId = event.getChannel().getIdLong();
            Long userId = event.getAuthor().getIdLong();
            String activeSession = sessionManager.getOrCreateSession(guildId, channelId, userId);
            sessionManager.forceReset(guildId, channelId, userId);
            messageRepository.deleteSession(activeSession);
            sessionSummaryRepository.deleteBySessionId(activeSession);
            event.replySuccess("Historial de conversación reiniciado para ti en este canal.");
        }
    }

    private static class StatusCmd extends AdminCommand {
        private final Bot bot;
        private final AIChatService aiChatService;

        public StatusCmd(Bot bot, AIChatService aiChatService, Category category) {
            super(category);
            this.bot = bot;
            this.aiChatService = aiChatService;
            this.name = "status";
            this.help = "Muestra el estado actual del chatbot de IA";
        }

        @Override
        public void execute(SlashCommandEvent event) {
            Server server = bot.getSettingsManager().getSettings(event.getGuild());
            event.replyEmbeds(buildStatusEmbed(aiChatService, server, event.getGuild().getName())).queue();
        }

        @Override
        public void execute(CommandEvent event) {
            Server server = bot.getSettingsManager().getSettings(event.getGuild());
            event.reply(buildStatusEmbed(aiChatService, server, event.getGuild().getName()));
        }
    }

    private static class ChannelCmd extends AdminCommand {
        private final Bot bot;

        public ChannelCmd(Bot bot, Category category) {
            super(category);
            this.bot = bot;
            this.name = "channel";
            this.help = "Establece o elimina el canal exclusivo para chatear con la IA";
            this.options = List.of(
                new OptionData(OptionType.CHANNEL, "canal-id", "Canal de texto para la IA. Si no se especifica, se elimina el canal exclusivo.").setRequired(false)
            );
        }

        @Override
        public void execute(SlashCommandEvent event) {
            Server server = bot.getSettingsManager().getSettings(event.getGuild());
            OptionMapping chOpt = event.getOption("canal-id");
            if (chOpt == null) {
                server.setAiChannelId(0L);
                server.save();
                event.reply("✅ **Canal exclusivo eliminado**. Ahora responderé a mis menciones en cualquier canal de texto.").queue();
            } else {
                server.setAiChannelId(chOpt.getAsChannel().getIdLong());
                server.save();
                event.reply("✅ **Canal exclusivo de IA establecido en**: " + chOpt.getAsChannel().getAsMention()).queue();
            }
        }

        @Override
        public void execute(CommandEvent event) {
            Server server = bot.getSettingsManager().getSettings(event.getGuild());
            String args = event.getArgs().trim();
            if (args.isEmpty()) {
                server.setAiChannelId(0L);
                server.save();
                event.replySuccess("Canal exclusivo eliminado. Responderé a menciones.");
            } else {
                String clean = args.replaceAll("[^0-9]", "");
                try {
                    long chId = Long.parseLong(clean);
                    TextChannel tc = event.getGuild().getTextChannelById(chId);
                    if (tc != null) {
                        server.setAiChannelId(chId);
                        server.save();
                        event.replySuccess("Canal exclusivo de IA establecido en: " + tc.getAsMention());
                    } else {
                        event.replyError("No se encontró el canal de texto proporcionado.");
                    }
                } catch (Exception e) {
                    event.replyError("ID de canal inválido.");
                }
            }
        }
    }

    private static class SetupCmd extends AdminCommand {
        private final Bot bot;
        private final AIChatService aiChatService;

        public SetupCmd(Bot bot, AIChatService aiChatService, Category category) {
            super(category);
            this.bot = bot;
            this.aiChatService = aiChatService;
            this.name = "setup";
            this.help = "Configura la API de IA (clave, URL, modelo) o una configuración de respaldo por índice";
            this.options = List.of(
                new OptionData(OptionType.STRING, "api-key", "Clave API de OpenAI/DeepSeek").setRequired(false),
                new OptionData(OptionType.STRING, "base-url", "Base URL personalizada").setRequired(false),
                new OptionData(OptionType.STRING, "modelo-ia", "Modelo de IA personalizado").setRequired(false),
                new OptionData(OptionType.INTEGER, "indice", "Índice de configuración de respaldo (1-10). Si se omite, se configura la clave del servidor.").setRequired(false)
            );
        }

        @Override
        public void execute(SlashCommandEvent event) {
            Server server = bot.getSettingsManager().getSettings(event.getGuild());
            OptionMapping keyOpt = event.getOption("api-key");
            OptionMapping urlOpt = event.getOption("base-url");
            OptionMapping modelOpt = event.getOption("modelo-ia");
            OptionMapping indexOpt = event.getOption("indice");

            Integer index = indexOpt != null ? (int) indexOpt.getAsLong() : null;

            if (index != null && (index < 1 || index > 10)) {
                event.reply("❌ El índice debe estar entre 1 y 10.").setEphemeral(true).queue();
                return;
            }

            String apiKey = keyOpt != null ? keyOpt.getAsString() : null;
            String baseUrl = urlOpt != null ? urlOpt.getAsString() : null;
            String model = modelOpt != null ? modelOpt.getAsString() : null;

            if (index != null) {
                aiChatService.saveServerBackupConfig(server.getId(), index, apiKey, baseUrl, model);
                if (apiKey != null && ("none".equalsIgnoreCase(apiKey) || "clear".equalsIgnoreCase(apiKey))) {
                    event.reply("✅ **Configuración de respaldo " + index + " eliminada con éxito.**").setEphemeral(true).queue();
                } else {
                    event.reply("🔒 **Configuración de respaldo " + index + " guardada de forma segura.**").setEphemeral(true).queue();
                }
            } else {
                if (keyOpt != null) server.setAiApiKey(apiKey);
                if (urlOpt != null) server.setAiBaseUrl(baseUrl);
                if (modelOpt != null) server.setAiModel(model);
                server.save();
                event.reply("🔒 **Configuración de IA privada guardada de forma segura**.")
                        .setEphemeral(true)
                        .queue();
            }
        }

        @Override
        public void execute(CommandEvent event) {
            String args = event.getArgs().trim();
            if (args.isEmpty()) {
                event.replyWarning("Uso: `ai setup [api-key=...] [base-url=...] [modelo-ia=...] [indice=...]` o `ai setup <apiKey> [baseUrl] [model]`");
                return;
            }

            String apiKey = extractValue(args, "api-key", "apikey");
            String baseUrl = extractValue(args, "base-url", "baseurl");
            String model = extractValue(args, "modelo-ia", "modelo", "model");
            String indexStr = extractValue(args, "indice", "index");

            if (apiKey != null || baseUrl != null || model != null || indexStr != null) {
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

                Server server = bot.getSettingsManager().getSettings(event.getGuild());
                if (index != null) {
                    if (apiKey != null && ("none".equalsIgnoreCase(apiKey) || "clear".equalsIgnoreCase(apiKey))) {
                        aiChatService.saveServerBackupConfig(server.getId(), index, apiKey, null, null);
                        event.replySuccess("Configuración de respaldo " + index + " eliminada con éxito.");
                    } else {
                        aiChatService.saveServerBackupConfig(server.getId(), index, apiKey, baseUrl, model);
                        event.replySuccess("Configuración de respaldo " + index + " guardada de forma segura.");
                    }
                } else {
                    if (apiKey != null) server.setAiApiKey(apiKey);
                    if (baseUrl != null) server.setAiBaseUrl(baseUrl);
                    if (model != null) server.setAiModel(model);
                    server.save();
                    event.replySuccess("Configuración de IA actualizada. Se recomienda borrar el mensaje original para proteger tu API Key.");
                }
            } else {
                String[] parts = args.split("\\s+");
                String posApiKey = parts.length >= 1 ? parts[0] : null;
                String posUrl = parts.length >= 2 ? parts[1] : null;
                String posModel = parts.length >= 3 ? parts[2] : null;

                if (posApiKey == null) {
                    event.replyWarning("Uso: `ai setup <apiKey> [baseUrl] [model]`");
                    return;
                }

                Server server = bot.getSettingsManager().getSettings(event.getGuild());
                server.setAiApiKey(posApiKey);
                if (posUrl != null) server.setAiBaseUrl(posUrl);
                if (posModel != null) server.setAiModel(posModel);
                server.save();
                event.replySuccess("Configuración de IA actualizada. Se recomienda borrar el mensaje original para proteger tu API Key.");
            }
        }

        private String extractValue(String args, String... keys) {
            for (String key : keys) {
                String prefix = key + "=";
                int idx = args.indexOf(prefix);
                if (idx != -1) {
                    int start = idx + prefix.length();
                    int end = args.length();
                    String[] allKeys = {"api-key=", "apikey=", "base-url=", "baseurl=", "modelo-ia=", "modelo=", "model=", "indice=", "index="};
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

    private static class ExclusiveCmd extends AdminCommand {
        private final Bot bot;

        public ExclusiveCmd(Bot bot, Category category) {
            super(category);
            this.bot = bot;
            this.name = "exclusive";
            this.help = "Activa o desactiva el modo exclusivo (IA solo responde en el canal dedicado)";
            this.options = List.of(
                new OptionData(OptionType.BOOLEAN, "exclusivo", "Habilita o deshabilita la respuesta exclusiva en el canal de IA").setRequired(true)
            );
        }

        @Override
        public void execute(SlashCommandEvent event) {
            Server server = bot.getSettingsManager().getSettings(event.getGuild());
            OptionMapping exclusiveOpt = event.getOption("exclusivo");
            if (exclusiveOpt == null) {
                event.reply("⚠️ Debes especificar el parámetro `exclusivo` (true/false) para esta acción.").setEphemeral(true).queue();
                return;
            }
            boolean exclusiveVal = exclusiveOpt.getAsBoolean();
            server.setAiExclusive(exclusiveVal);
            server.save();
            if (exclusiveVal) {
                event.reply("🔒 **Respuestas exclusivas activadas**. Ahora la IA solo responderá a mensajes enviados en el canal exclusivo de IA y no atenderá menciones/respuestas en otros canales.").queue();
            } else {
                event.reply("🔓 **Respuestas exclusivas desactivadas**. La IA responderá en el canal exclusivo y a menciones/respuestas en otros canales.").queue();
            }
        }

        @Override
        public void execute(CommandEvent event) {
            Server server = bot.getSettingsManager().getSettings(event.getGuild());
            String args = event.getArgs().trim();
            if (args.isEmpty()) {
                event.replyWarning("Uso: `ai exclusive <true|false>`");
                return;
            }
            String excStr = args.toLowerCase();
            if ("true".equals(excStr) || "yes".equals(excStr) || "habilitado".equals(excStr) || "enable".equals(excStr)) {
                server.setAiExclusive(true);
                server.save();
                event.replySuccess("Respuestas exclusivas activadas. La IA solo responderá en el canal exclusivo.");
            } else if ("false".equals(excStr) || "no".equals(excStr) || "deshabilitado".equals(excStr) || "disable".equals(excStr)) {
                server.setAiExclusive(false);
                server.save();
                event.replySuccess("Respuestas exclusivas desactivadas. La IA responderá en otros canales al ser mencionada.");
            } else {
                event.replyError("Valor no válido. Elige `true` o `false`.");
            }
        }
    }
}
