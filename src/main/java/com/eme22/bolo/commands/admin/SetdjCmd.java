package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.utils.FormatUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import java.util.Collections;
import java.util.List;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class SetdjCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.setdj", defaultValue = "")
   String[] aliases = new String[0];

   public SetdjCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "setdj";
      this.help = "actualiza el rol de DJ";
      this.arguments = "<rolename|NONE>";
      this.options = Collections.singletonList(new OptionData(OptionType.ROLE, "rol", "rol a poner de dj. Ponga @Everyone para limpiar").setRequired(true));
   }

   protected void execute(SlashCommandEvent event) {
      Role role = event.getOption("rol").getAsRole();
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (role.getIdLong() == event.getGuild().getIdLong()) {
         s.setDjRoleId(0L);
         event.reply(event.getClient().getSuccess() + " Rol de DJ limpiado. Todos pueden usar los comandos de DJ.").queue();
      } else {
         s.setDjRoleId(role.getIdLong());
         event.reply(event.getClient().getSuccess() + " Los comandos de dj ahora pueden ser usados por usuarios con el rol **" + role.getAsMention() + "**.")
            .queue();
      }

      s.persist();
   }

   protected void execute(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.reply(event.getClient().getError() + " Please include a role name or NONE");
      } else {
         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         if (event.getArgs().equalsIgnoreCase("none")) {
            s.setDjRoleId(0L);
            event.reply(event.getClient().getSuccess() + " DJ role cleared; Only Admins can use the DJ commands.");
         } else {
            List<Role> list = FinderUtil.findRoles(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
               event.reply(event.getClient().getWarning() + " No Roles found matching \"" + event.getArgs() + "\"");
            } else if (list.size() > 1) {
               event.reply(event.getClient().getWarning() + FormatUtil.listOfRoles(list, event.getArgs()));
            } else {
               s.setDjRoleId(list.get(0).getIdLong());
               event.reply(event.getClient().getSuccess() + " DJ commands can now be used by users with the **" + list.get(0).getName() + "** role.");
            }
         }

         s.persist();
      }
   }
}



