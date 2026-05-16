package com.eme22.bolo.commands.admin;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.Collections;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class AntiRaidModeCmd extends AdminCommand {
   private final Bot bot;
   @ConfigProperty(name = "config.aliases.enableantiraidmode", defaultValue = "")
   String[] aliases = new String[0];

   public AntiRaidModeCmd(Bot bot, @Named("adminCategory") Category category) {
      super(category);
      this.bot = bot;
      this.name = "enableantiraidmode";
      this.help = "modo anti raid <on> <off>";
      this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, "estado", "activa o desactiva el modo anti raid.").setRequired(true));
   }

   public void execute(SlashCommandEvent event) {
      OptionMapping canal = event.getOption("estado");
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (canal != null && canal.getAsBoolean()) {
         s.setAntiRaidMode(true);
         this.makeServerSafe();
      } else {
         s.setAntiRaidMode(false);
      }

      s.persist();
   }

   public void execute(CommandEvent event) {
      String canal = event.getArgs();
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      if (canal != null && canal.equals("on")) {
         s.setAntiRaidMode(true);
         s.persist();
         this.makeServerSafe();
      } else if (canal != null && canal.equals("off")) {
         s.setAntiRaidMode(false);
         s.persist();
      }
   }

   private void makeServerSafe() {
      this.bot.getJDA().getRoles().forEach(role -> {
         if (role.hasPermission(new Permission[]{Permission.MANAGE_CHANNEL})) {
            role.getManager().revokePermissions(new Permission[]{Permission.MANAGE_CHANNEL});
         }
      });
   }
}











