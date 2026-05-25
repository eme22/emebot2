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
                .addChoice("mode", "mode")
                .addChoice("setup", "setup")
                .setRequired(true);

        OptionData channelOption = new OptionData(OptionType.CHANNEL, "canal-id", "Canal exclusivo para chatear con la IA (acción 'channel').");
        OptionData modeOption = new OptionData(OptionType.STRING, "modo-ia", "Modo operativo de la IA (acción 'mode').")
                .addChoice("normal", "normal")
                .addChoice("admin", "admin");
        OptionData apiKeyOption = new OptionData(OptionType.STRING, "api-key", "Clave API de OpenAI/DeepSeek (acción 'setup').");
        OptionData baseUrlOption = new OptionData(OptionType.STRING, "base-url", "Base URL personalizada (acción 'setup').");
        OptionData modelOption = new OptionData(OptionType.STRING, "modelo-ia", "Modelo de IA personalizado (acción 'setup').");

        this.options = Arrays.asList(actionOption, channelOption, modeOption, apiKeyOption, baseUrlOption, modelOption);
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

            case "mode":
                OptionMapping modeOpt = event.getOption("modo-ia");
                if (modeOpt == null) {
                    event.reply("⚠️ Debes especificar el parámetro `modo-ia` para esta acción.").setEphemeral(true).queue();
                    return;
                }
                String targetMode = modeOpt.getAsString().toUpperCase();
                server.setAiMode(targetMode);
                server.save();
                event.reply("🤖 **Modo de IA cambiado a**: `" + targetMode + "`").queue();
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

            case "mode":
                if (args.length < 2) {
                    event.replyWarning("Uso: `ai mode <normal|admin>`");
                    return;
                }
                String m = args[1].toUpperCase();
                if ("NORMAL".equals(m) || "ADMIN".equals(m)) {
                    server.setAiMode(m);
                    server.save();
                    event.replySuccess("Modo de IA cambiado a: `" + m + "`");
                } else {
                    event.replyError("Modo no válido. Elige `normal` o `admin`.");
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

            default:
                event.replyWarning("Acción desconocida. Usa `enable`, `disable`, `channel`, `mode`, `reset` o `status`.");
                break;
        }
    }

    private net.dv8tion.jda.api.entities.MessageEmbed buildStatusEmbed(Server server, String guildName) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(0x00A2E8);
        builder.setTitle("🤖 Estado del Chatbot de IA - " + guildName);
        builder.setTimestamp(Instant.now());

        builder.addField("Habilitado", server.isAiEnabled() ? "🟢 Sí" : "🔴 No", true);
        builder.addField("Modo Activo", "`" + (server.getAiMode() != null ? server.getAiMode() : "NORMAL") + "`", true);

        long channelId = server.getAiChannelId();
        String channelStr = channelId == 0L ? "Ninguno (Responder a Menciones `@mention`)" : "<#" + channelId + ">";
        builder.addField("Canal Dedicado", channelStr, false);

        String model = server.getAiModel() != null ? server.getAiModel() : "Global Default (gpt-4o-mini)";
        String baseUrl = server.getAiBaseUrl() != null ? server.getAiBaseUrl() : "Global Default (OpenAI)";
        builder.addField("Modelo", "`" + model + "`", true);
        builder.addField("Proveedor URL Base", "`" + baseUrl + "`", true);
        builder.addField("Clave API Personalizada", server.getAiApiKey() != null ? "🔒 Configurada" : "❌ No (Usa global)", false);

        builder.setFooter("EMBot AI Subsystem");
        return builder.build();
    }
}
