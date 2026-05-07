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
public class SetAdminCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.setadmin", defaultValue = "")
   String[] aliases = new String[0];

   public SetAdminCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "setadmin";
      this.help = "actualiza el rol de Admin";
      this.arguments = "<rolename|NONE>";
      this.options = Collections.singletonList(new OptionData(OptionType.ROLE, "rol", "rol a poner de admib. Ponga @Everyone para limpiar").setRequired(true));
   }

   protected void execute(SlashCommandEvent event) {
      Role role = event.getOption("rol").getAsRole();
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (role.getIdLong() == event.getGuild().getIdLong()) {
         s.setAdminRoleId(0L);
         event.reply(event.getClient().getSuccess() + "Rol de admin limpiado. Solo el creador del servidor puede usar los comandos de admin.").queue();
      } else {
         s.setAdminRoleId(role.getIdLong());
         event.reply(event.getClient().getSuccess() + " Los comandos de admin ahora pueden ser usados por usuarios con el rol **" + role.getAsMention() + "**.")
            .queue();
      }

      s.persist();
   }

   protected void execute(CommandEvent event) {
      try {
         if (event.getArgs().isEmpty()) {
            event.replyError(" Ponga un rol o NONE para ninguno");
            return;
         }

         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         if (event.getArgs().equalsIgnoreCase("none")) {
            s.setAdminRoleId(0L);
            event.replySuccess(" Rol de admin limpiado. Solo el creador del servidor puede usar los comandos de admin.");
         } else {
            List<Role> list = FinderUtil.findRoles(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
               event.replyWarning(" No Roles found matching \"" + event.getArgs() + "\"");
            } else if (list.size() > 1) {
               event.replyWarning(FormatUtil.listOfRoles(list, event.getArgs()));
            } else {
               s.setAdminRoleId(list.get(0).getIdLong());
               event.replySuccess(" Los comandos de admin ahora pueden ser usados por usuarios con el rol **" + list.get(0).getName() + "** role.");
            }
         }

         s.persist();
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }
}



