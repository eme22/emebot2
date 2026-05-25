package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.model.UserOffense;
import com.eme22.bolo.services.UserOffenseService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import java.util.Collections;
import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class OffensesCmd extends BaseCommand {

    private final Bot bot;
    private final UserOffenseService offenseService;

    @Inject
    public OffensesCmd(Bot bot, UserOffenseService offenseService) {
        this.bot = bot;
        this.offenseService = offenseService;
        this.name = "offenses";
        this.help = "Muestra la cantidad de ofensas de un usuario y su estado de bloqueo.";
        this.aliases = new String[]{"ofensas", "warns"};
        this.guildOnly = true;
        this.options = Collections.singletonList(
            new OptionData(OptionType.USER, "usuario", "Selecciona al usuario para ver sus ofensas (Solo Admins).").setRequired(false)
        );
    }

    private boolean canViewOthers(Member member) {
        if (member == null) return false;
        return member.isOwner() 
            || member.hasPermission(Permission.ADMINISTRATOR) 
            || member.getId().equals(bot.getJDA().getSelfUser().getId())
            || member.getId().equals(member.getGuild().getSelfMember().getId());
    }

    private void sendOffensesInfo(Object eventObj, User targetUser) {
        UserOffense offense = offenseService.getOrCreateOffenses(targetUser.getIdLong());
        boolean banned = offenseService.isBanned(targetUser.getIdLong());

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("⚠️ **Historial de Ofensas de %s**:\n", targetUser.getAsMention()));
        sb.append(String.format("- Ofensas totales: `%d` / `5` para bloqueo\n", offense.getOffenseCount()));
        
        if (offense.getLastOffenseTimestamp() != null) {
            sb.append(String.format("- Última ofensa: <t:%d:F> (<t:%d:R>)\n", 
                offense.getLastOffenseTimestamp().getEpochSecond(), 
                offense.getLastOffenseTimestamp().getEpochSecond()));
        }
        
        sb.append("- Estado actual: ");
        if (banned) {
            long epochSec = offense.getBanUntil().getEpochSecond();
            sb.append(String.format("🚫 **BLOQUEADO** hasta <t:%d:F> (<t:%d:R>)", epochSec, epochSec));
        } else {
            sb.append("✅ **ACTIVO** (Sin bloqueos)");
        }

        if (eventObj instanceof SlashCommandEvent slashEvent) {
            slashEvent.reply(sb.toString()).queue();
        } else if (eventObj instanceof CommandEvent textEvent) {
            textEvent.reply(sb.toString());
        }
    }

    @Override
    public void execute(SlashCommandEvent event) {
        OptionMapping option = event.getOption("usuario");
        User targetUser;
        if (option != null) {
            targetUser = option.getAsUser();
            if (targetUser.getIdLong() != event.getUser().getIdLong() && !canViewOthers(event.getMember())) {
                event.reply("❌ No tienes permisos de administrador para consultar las ofensas de otros usuarios.").setEphemeral(true).queue();
                return;
            }
        } else {
            targetUser = event.getUser();
        }

        sendOffensesInfo(event, targetUser);
    }

    @Override
    public void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            sendOffensesInfo(event, event.getAuthor());
        } else {
            List<Member> members = FinderUtil.findMembers(event.getArgs(), event.getGuild());
            if (members.isEmpty()) {
                try {
                    long id = Long.parseLong(event.getArgs().replaceAll("[^0-9]", ""));
                    event.getJDA().retrieveUserById(id).queue(user -> {
                        if (user.getIdLong() != event.getAuthor().getIdLong() && !canViewOthers(event.getMember())) {
                            event.replyError("❌ No tienes permisos de administrador para consultar las ofensas de otros usuarios.");
                        } else {
                            sendOffensesInfo(event, user);
                        }
                    }, throwable -> {
                        event.replyError("❌ Usuario no encontrado.");
                    });
                } catch (NumberFormatException e) {
                    event.replyError("❌ Usuario no encontrado.");
                }
            } else {
                Member targetMember = members.get(0);
                if (targetMember.getUser().getIdLong() != event.getAuthor().getIdLong() && !canViewOthers(event.getMember())) {
                    event.replyError("❌ No tienes permisos de administrador para consultar las ofensas de otros usuarios.");
                } else {
                    sendOffensesInfo(event, targetMember.getUser());
                }
            }
        }
    }
}
