package com.eme22.bolo.commands.admin;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.RoleManager;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class SetRoleManagerCmd extends AdminCommand {
   protected final Bot bot;
   @ConfigProperty(name = "config.aliases.rolemsgbuild", defaultValue = "")
   String[] aliases = new String[0];

   public SetRoleManagerCmd(Bot bot, @Named("adminCategory") Category category) {
      super(category);
      this.bot = bot;
      this.name = "rolemsgbuild";
      this.help = "crea un mensaje con un menú desplegable en el cual los usuarios pueden seleccionar roles";
      this.arguments = "[Mensaje] rol rol... rol";
      this.options = Arrays.asList(
         new OptionData(OptionType.STRING, "mensaje", "mensaje a enviar como selector de roles").setRequired(true),
         new OptionData(OptionType.ROLE, "role1", "rol 1 a dar al usuario").setRequired(true),
         new OptionData(OptionType.BOOLEAN, "exclusivo", "si es verdadero, solo se puede elegir un rol de la lista a la vez").setRequired(false),
         new OptionData(OptionType.ROLE, "role2", "rol 2 a dar al usuario").setRequired(false),
         new OptionData(OptionType.ROLE, "role3", "rol 3 a dar al usuario").setRequired(false),
         new OptionData(OptionType.ROLE, "role4", "rol 4 a dar al usuario").setRequired(false),
         new OptionData(OptionType.ROLE, "role5", "rol 5 a dar al usuario").setRequired(false),
         new OptionData(OptionType.ROLE, "role6", "rol 6 a dar al usuario").setRequired(false),
         new OptionData(OptionType.ROLE, "role7", "rol 7 a dar al usuario").setRequired(false),
         new OptionData(OptionType.ROLE, "role8", "rol 8 a dar al usuario").setRequired(false),
         new OptionData(OptionType.ROLE, "role9", "rol 9 a dar al usuario").setRequired(false),
         new OptionData(OptionType.ROLE, "role10", "rol 10 a dar al usuario").setRequired(false)
      );
   }

   public void execute(SlashCommandEvent event) {
      String message = event.optString("mensaje");
      boolean toggled = event.optBoolean("exclusivo", false);
      List<Role> roles = new ArrayList<>();
      List<OptionMapping> all = event.getOptions();
      
      all.forEach(optionMapping -> {
         if (optionMapping.getType().equals(OptionType.ROLE)) {
            roles.add(optionMapping.getAsRole());
         }
      });

      if (roles.isEmpty()) {
         event.reply(event.getClient().getError() + " Por favor incluya correctamente los roles").setEphemeral(true).queue();
      } else {
         RoleManager manager = new RoleManager();
         EmbedBuilder eb = new EmbedBuilder();
         eb.setDescription(message);
         eb.setColor(event.getGuild().getSelfMember().getColor());

         StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("roleselect")
            .setPlaceholder("Selecciona tus roles...")
            .setMinValues(0)
            .setMaxValues(toggled ? 1 : roles.size());

         HashMap<String, String> map = new HashMap<>();
         for (Role role : roles) {
            if (!event.getGuild().getSelfMember().canInteract(role)) {
               event.reply(event.getClient().getError() + " El rol " + role.getAsMention() + " es mayor que el rol del bot, no se puede agregar").setEphemeral(true).queue();
               return;
            }
            menuBuilder.addOption(role.getName(), role.getId());
            map.put(role.getId(), role.getAsMention());
         }

         StringSelectMenu menu = menuBuilder.build();

         event.getTextChannel().sendMessageEmbeds(eb.build(), new MessageEmbed[0])
            .setComponents(ActionRow.of(menu))
            .queue(success -> {
               manager.setId(success.getIdLong());
               manager.setEmoji(map);
               manager.setToggled(toggled);

               try {
                  Server server = this.bot.getSettingsManager().getSettings(event.getGuild().getIdLong());
                  server.addToRoleManagers(manager);
                  server.persist();
               } catch (Exception var10) {
                  success.delete().queue();
                  event.reply(event.getClient().getError() + " Error al guardar el administrador de roles: " + var10.getMessage()).setEphemeral(true).queue();
                  return;
               }

               event.reply(event.getClient().getSuccess() + " ¡Administrador de roles creado con éxito!").setEphemeral(true).queue();
            });
      }
   }

   public void execute(CommandEvent event) {
      String[] args = event.getArgs().split("] ");
      if (args.length < 2) {
         event.replyError(" Por favor incluya al menos un mensaje y un rol");
      } else {
         String message = args[0].substring(1);
         String[] rolesArgs = args[1].split(" ");
         if (rolesArgs.length >= 1) {
            RoleManager manager = new RoleManager();
            EmbedBuilder eb = new EmbedBuilder();
            eb.setDescription(message);
            eb.setColor(event.getGuild().getSelfMember().getColor());

            ArrayList<Role> roles = new ArrayList<>();
            for (String arg : rolesArgs) {
               List<Role> found = FinderUtil.findRoles(arg.trim(), event.getGuild());
               if (!found.isEmpty()) {
                  roles.add(found.get(0));
               }
            }

            if (roles.isEmpty()) {
               event.replyError(" No se encontraron roles válidos");
               return;
            }

            StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("roleselect")
               .setPlaceholder("Selecciona tus roles...")
               .setMinValues(0)
               .setMaxValues(roles.size());

            HashMap<String, String> map = new HashMap<>();
            for (Role role : roles) {
               if (!event.getGuild().getSelfMember().canInteract(role)) {
                  event.replyError("El rol " + role.getAsMention() + " es mayor que el rol del bot, no se puede agregar");
                  return;
               }
               menuBuilder.addOption(role.getName(), role.getId());
               map.put(role.getId(), role.getAsMention());
            }

            StringSelectMenu menu = menuBuilder.build();

            event.getChannel().sendMessageEmbeds(eb.build(), new MessageEmbed[0])
               .setComponents(ActionRow.of(menu))
               .queue(success -> {
                  manager.setId(success.getIdLong());
                  manager.setEmoji(map);
                  manager.setToggled(false);

                  try {
                     Server server = this.bot.getSettingsManager().getSettings(event.getGuild().getIdLong());
                     server.addToRoleManagers(manager);
                     server.persist();
                  } catch (Exception var7) {
                     success.delete().queue();
                     event.replyError(" Error al guardar el administrador de roles: " + var7.getMessage());
                     return;
                  }

                  event.getMessage().delete().queue();
               });
         } else {
            event.replyError(" Por favor incluya correctamente los roles");
         }
      }
   }
}
