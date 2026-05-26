package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
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
import java.util.Arrays;

@Singleton
@Transactional
@ActivateRequestContext
public class AIChatCmd extends AdminCommand {

    private final Bot bot;
    private final AIChatSessionManager sessionManager;
    private final AIChatMessageRepository messageRepository;

    @Inject
    public AIChatCmd(Bot bot, AIChatSessionManager sessionManager, AIChatMessageRepository messageRepository, @Named("adminCategory") Category category) {
        super(category);
        this.bot = bot;
        this.sessionManager = sessionManager;
        this.messageRepository = messageRepository;
        this.name = "ai";
        this.help = "Configura y administra el chatbot de IA del servidor";

        // Register Slash Options
        OptionData actionOption = new OptionData(OptionType.STRING, "accion", "Acción a realizar.")
                .addChoice("enable", "enable")
                .addChoice("disable", "disable")
                .addChoice("reset", "reset")
                .addChoice("status", "status")
                .addChoice("channel", "channel")
                .addChoice("setup", "setup")
                .addChoice("exclusive", "exclusive")
                .setRequired(true);

        OptionData channelOption = new OptionData(OptionType.CHANNEL, "canal-id", "Canal exclusivo para chatear con la IA (acción 'channel').");
        OptionData apiKeyOption = new OptionData(OptionType.STRING, "api-key", "Clave API de OpenAI/DeepSeek (acción 'setup').");
        OptionData baseUrlOption = new OptionData(OptionType.STRING, "base-url", "Base URL personalizada (acción 'setup').");
        OptionData modelOption = new OptionData(OptionType.STRING, "modelo-ia", "Modelo de IA personalizado (acción 'setup').");
        OptionData exclusiveOption = new OptionData(OptionType.BOOLEAN, "exclusivo", "Habilita o deshabilita la respuesta exclusiva en el canal de IA (acción 'exclusive').");

        this.options = Arrays.asList(actionOption, channelOption, apiKeyOption, baseUrlOption, modelOption, exclusiveOption);
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String action = event.getOption("accion").getAsString().toLowerCase();
        Server server = bot.getSettingsManager().getSettings(event.getGuild());
        Long guildId = event.getGuild().getIdLong();

        switch (action) {
            case "enable":
                server.setAiEnabled(true);
                server.save();
                event.reply("✅ **Chatbot de IA Habilitado**. Ahora puedes chatear conmigo en el servidor.").queue();
                break;

            case "disable":
                server.setAiEnabled(false);
                server.save();
                event.reply("❌ **Chatbot de IA Deshabilitado**.").queue();
                break;

            case "reset":
                Long channelId = event.getChannel().getIdLong();
                Long userId = event.getUser().getIdLong();
                String activeSession = sessionManager.getOrCreateSession(guildId, channelId, userId);
                sessionManager.forceReset(guildId, channelId, userId);
                messageRepository.deleteSession(activeSession);
                event.reply("🔄 **Historial de conversación reiniciado**. He olvidado nuestro contexto previo en este canal. ¡Empecemos de nuevo!").queue();
                break;

            case "channel":
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
                break;

            case "setup":
                OptionMapping keyOpt = event.getOption("api-key");
                OptionMapping urlOpt = event.getOption("base-url");
                OptionMapping modelOpt = event.getOption("modelo-ia");

                if (keyOpt != null) server.setAiApiKey(keyOpt.getAsString());
                if (urlOpt != null) server.setAiBaseUrl(urlOpt.getAsString());
                if (modelOpt != null) server.setAiModel(modelOpt.getAsString());

                server.save();
                event.reply("🔒 **Configuración de IA privada guardada de forma segura**.")
                        .setEphemeral(true)
                        .queue();
                break;

            case "exclusive":
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
                break;

            case "status":
            default:
                event.replyEmbeds(buildStatusEmbed(server, event.getGuild().getName())).queue();
                break;
        }
    }

    @Override
    public void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\\s+");
        String action = args[0].toLowerCase();
        Server server = bot.getSettingsManager().getSettings(event.getGuild());
        Long guildId = event.getGuild().getIdLong();

        if (action.isEmpty() || "status".equals(action)) {
            event.reply(buildStatusEmbed(server, event.getGuild().getName()));
            return;
        }

        switch (action) {
            case "enable":
                server.setAiEnabled(true);
                server.save();
                event.replySuccess("Chatbot de IA Habilitado.");
                break;

            case "disable":
                server.setAiEnabled(false);
                server.save();
                event.replySuccess("Chatbot de IA Deshabilitado.");
                break;

            case "reset":
                Long channelId = event.getChannel().getIdLong();
                Long userId = event.getAuthor().getIdLong();
                String activeSession = sessionManager.getOrCreateSession(guildId, channelId, userId);
                sessionManager.forceReset(guildId, channelId, userId);
                messageRepository.deleteSession(activeSession);
                event.replySuccess("Historial de conversación reiniciado para ti en este canal.");
                break;

            case "channel":
                if (args.length < 2) {
                    server.setAiChannelId(0L);
                    server.save();
                    event.replySuccess("Canal exclusivo eliminado. Responderé a menciones.");
                } else {
                    String clean = args[1].replaceAll("[^0-9]", "");
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
                break;

            case "setup":
                if (args.length < 2) {
                    event.replyWarning("Uso: `ai setup <apiKey> [baseUrl] [model]`");
                    return;
                }
                server.setAiApiKey(args[1]);
                if (args.length >= 3) server.setAiBaseUrl(args[2]);
                if (args.length >= 4) server.setAiModel(args[3]);
                server.save();
                event.replySuccess("Configuración de IA actualizada. Se recomienda borrar el mensaje original para proteger tu API Key.");
                break;

            case "exclusive":
                if (args.length < 2) {
                    event.replyWarning("Uso: `ai exclusive <true|false>`");
                    return;
                }
                String excStr = args[1].toLowerCase();
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
                break;

            default:
                event.replyWarning("Acción desconocida. Usa `enable`, `disable`, `channel`, `exclusive`, `reset` o `status`.");
                break;
        }
    }

    private net.dv8tion.jda.api.entities.MessageEmbed buildStatusEmbed(Server server, String guildName) {
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

        builder.setFooter("EMBot AI Subsystem");
        return builder.build();
    }
}
