package com.eme22.bolo.commands.admin;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.UserOffense;
import com.eme22.bolo.services.UserOffenseService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import java.util.Arrays;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class ManageOffensesCmd extends AdminCommand {

    private final Bot bot;
    private final UserOffenseService offenseService;

    @Inject
    public ManageOffensesCmd(Bot bot, UserOffenseService offenseService, @Named("adminCategory") Category category) {
        super(category);
        this.bot = bot;
        this.offenseService = offenseService;
        this.name = "manageoffenses";
        this.help = "Permite administrar las ofensas y bloqueos de los usuarios.";
        this.aliases = new String[]{"mofensas", "warnmanage"};
        this.guildOnly = true;

        OptionData actionOption = new OptionData(OptionType.STRING, "accion", "Acción a realizar.")
                .addChoice("add", "add")
                .addChoice("clear", "clear")
                .addChoice("set", "set")
                .setRequired(true);
        OptionData userOption = new OptionData(OptionType.USER, "usuario", "El usuario a gestionar.").setRequired(true);
        OptionData countOption = new OptionData(OptionType.INTEGER, "cantidad", "Cantidad de ofensas (solo para acción 'set').");

        this.options = Arrays.asList(actionOption, userOption, countOption);
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String action = event.getOption("accion").getAsString().toLowerCase();
        User user = event.getOption("usuario").getAsUser();

        switch (action) {
            case "add":
                UserOffense offense = offenseService.addOffense(user.getIdLong());
                boolean banned = offenseService.isBanned(user.getIdLong());
                String banAlert = "";
                if (banned) {
                    banAlert = String.format(" ¡Bloqueado temporalmente hasta <t:%d:F>!", offense.getBanUntil().getEpochSecond());
                }
                event.reply(String.format("✅ Se ha añadido una ofensa a %s. Total actual: `%d` / `5`.%s", 
                    user.getAsMention(), offense.getOffenseCount(), banAlert)).queue();
                break;

            case "clear":
                offenseService.clearOffenses(user.getIdLong());
                event.reply(String.format("✅ Se han limpiado todas las ofensas y se ha desbloqueado a %s.", user.getAsMention())).queue();
                break;

            case "set":
                OptionMapping countOpt = event.getOption("cantidad");
                if (countOpt == null) {
                    event.reply("❌ Debes especificar el parámetro `cantidad` para la acción 'set'.").setEphemeral(true).queue();
                    return;
                }
                int count = (int) countOpt.getAsLong();
                if (count < 0) {
                    event.reply("❌ La cantidad de ofensas no puede ser menor a 0.").setEphemeral(true).queue();
                    return;
                }
                offenseService.setOffenseCount(user.getIdLong(), count);
                UserOffense setOffense = offenseService.getOrCreateOffenses(user.getIdLong());
                boolean setBanned = offenseService.isBanned(user.getIdLong());
                String setBanAlert = "";
                if (setBanned) {
                    setBanAlert = String.format(" ¡Bloqueado temporalmente hasta <t:%d:F>!", setOffense.getBanUntil().getEpochSecond());
                }
                event.reply(String.format("✅ Se han establecido las ofensas de %s en `%d`.%s", 
                    user.getAsMention(), count, setBanAlert)).queue();
                break;

            default:
                event.reply("❌ Acción no válida.").setEphemeral(true).queue();
                break;
        }
    }

    @Override
    public void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\\s+");
        if (args.length < 2 || args[0].trim().isEmpty()) {
            event.replyWarning("Uso: `manageoffenses <add|clear|set> <usuario> [cantidad]`");
            return;
        }

        String action = args[0].toLowerCase();
        String userArg = args[1];

        List<Member> members = FinderUtil.findMembers(userArg, event.getGuild());
        if (members.isEmpty()) {
            try {
                long id = Long.parseLong(userArg.replaceAll("[^0-9]", ""));
                event.getJDA().retrieveUserById(id).queue(user -> {
                    processTextAction(event, action, user, args);
                }, throwable -> {
                    event.replyError("❌ Usuario no encontrado.");
                });
            } catch (NumberFormatException e) {
                event.replyError("❌ Usuario no encontrado.");
            }
        } else {
            processTextAction(event, action, members.get(0).getUser(), args);
        }
    }

    private void processTextAction(CommandEvent event, String action, User user, String[] args) {
        switch (action) {
            case "add":
                UserOffense offense = offenseService.addOffense(user.getIdLong());
                boolean banned = offenseService.isBanned(user.getIdLong());
                String banAlert = "";
                if (banned) {
                    banAlert = String.format(" ¡Bloqueado temporalmente hasta <t:%d:F>!", offense.getBanUntil().getEpochSecond());
                }
                event.replySuccess(String.format("Se ha añadido una ofensa a %s. Total actual: `%d` / `5`.%s", 
                    user.getAsMention(), offense.getOffenseCount(), banAlert));
                break;

            case "clear":
                offenseService.clearOffenses(user.getIdLong());
                event.replySuccess(String.format("Se han limpiado todas las ofensas y se ha desbloqueado a %s.", user.getAsMention()));
                break;

            case "set":
                if (args.length < 3) {
                    event.replyError("Debes especificar la cantidad de ofensas.");
                    return;
                }
                try {
                    int count = Integer.parseInt(args[2]);
                    if (count < 0) {
                        event.replyError("La cantidad de ofensas no puede ser menor a 0.");
                        return;
                    }
                    offenseService.setOffenseCount(user.getIdLong(), count);
                    UserOffense setOffense = offenseService.getOrCreateOffenses(user.getIdLong());
                    boolean setBanned = offenseService.isBanned(user.getIdLong());
                    String setBanAlert = "";
                    if (setBanned) {
                        setBanAlert = String.format(" ¡Bloqueado temporalmente hasta <t:%d:F>!", setOffense.getBanUntil().getEpochSecond());
                    }
                    event.replySuccess(String.format("Se han establecido las ofensas de %s en `%d`.%s", 
                        user.getAsMention(), count, setBanAlert));
                } catch (NumberFormatException e) {
                    event.replyError("Cantidad inválida.");
                }
                break;

            default:
                event.replyError("Acción desconocida. Usa `add`, `clear` o `set`.");
                break;
        }
    }
}
