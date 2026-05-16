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
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class SetRoleManagerToggledCmd extends AdminCommand {
   protected final Bot bot;
   @ConfigProperty(name = "config.aliases.rolemsgtoggledbuild", defaultValue = "")
   String[] aliases = new String[0];

   public SetRoleManagerToggledCmd(Bot bot, @Named("adminCategory") Category category) {
      super(category);
      this.bot = bot;
      this.name = "rolemsgtoggledbuild";
      this.help = "crea un mensaje en el cual los usuarios pueden reaccionar para obtener un rol determinado";
      this.arguments = "[Mensaje] emoji rol emoji rol... emoji rol";
      this.options = Arrays.asList(
         new OptionData(OptionType.STRING, "mensaje", "mensaje a enviar como selector de roles").setRequired(true),
         new OptionData(OptionType.STRING, "emoji1", "emoji del rol 1").setRequired(true),
         new OptionData(OptionType.ROLE, "role1", "rol a dar al usuario que reaccione").setRequired(true),
         new OptionData(OptionType.STRING, "emoji2", "emoji del rol 1").setRequired(false),
         new OptionData(OptionType.ROLE, "role2", "rol a dar al usuario que reaccione").setRequired(false),
         new OptionData(OptionType.STRING, "emoji3", "emoji del rol 1").setRequired(false),
         new OptionData(OptionType.ROLE, "role3", "rol a dar al usuario que reaccione").setRequired(false),
         new OptionData(OptionType.STRING, "emoji4", "emoji del rol 1").setRequired(false),
         new OptionData(OptionType.ROLE, "role4", "rol a dar al usuario que reaccione").setRequired(false),
         new OptionData(OptionType.STRING, "emoji5", "emoji del rol 1").setRequired(false),
         new OptionData(OptionType.ROLE, "role5", "rol a dar al usuario que reaccione").setRequired(false),
         new OptionData(OptionType.STRING, "emoji6", "emoji del rol 1").setRequired(false),
         new OptionData(OptionType.ROLE, "role6", "rol a dar al usuario que reaccione").setRequired(false),
         new OptionData(OptionType.STRING, "emoji7", "emoji del rol 1").setRequired(false),
         new OptionData(OptionType.ROLE, "role7", "rol a dar al usuario que reaccione").setRequired(false),
         new OptionData(OptionType.STRING, "emoji8", "emoji del rol 1").setRequired(false),
         new OptionData(OptionType.ROLE, "role8", "rol a dar al usuario que reaccione").setRequired(false),
         new OptionData(OptionType.STRING, "emoji9", "emoji del rol 1").setRequired(false),
         new OptionData(OptionType.ROLE, "role9", "rol a dar al usuario que reaccione").setRequired(false),
         new OptionData(OptionType.STRING, "emoji10", "emoji del rol 1").setRequired(false),
         new OptionData(OptionType.ROLE, "role10", "rol a dar al usuario que reaccione").setRequired(false)
      );
   }

   public void execute(SlashCommandEvent event) {
      String message = event.optString("mensaje");
      List<String> emojis = new ArrayList<>();
      List<Role> roles = new ArrayList<>();
      List<OptionMapping> all = event.getOptions();
      all.forEach(optionMapping -> {
         if (!optionMapping.getName().equals("mensaje")) {
            if (optionMapping.getType().equals(OptionType.STRING)) {
               emojis.add(optionMapping.getAsString());
            } else {
               roles.add(optionMapping.getAsRole());
            }
         }
      });
      if (emojis.size() != roles.size()) {
         event.reply(event.getClient().getError() + " Por favor incluya correctamente los emojis y roles").setEphemeral(true).queue();
      } else {
         RoleManager manager = new RoleManager();
         EmbedBuilder eb = new EmbedBuilder();
         eb.setDescription(message);
         event.getTextChannel().sendMessageEmbeds(eb.build(), new MessageEmbed[0]).queue(success -> {
            HashMap<String, String> map = new HashMap<>();

            for (int i = 0; i < emojis.size(); i++) {
               String emoji = emojis.get(i);
               if (emoji.startsWith("<")) {
                  emoji = emoji.substring(2, emoji.length() - 1);
               }

               success.addReaction(Emoji.fromFormatted(emoji)).queue();
               Role role = roles.get(i);
               if (!event.getGuild().getSelfMember().canInteract(role)) {
                  success.delete().queue();
                  event.reply(event.getClient().getError() + " El rol " + role.getAsMention() + " es mayor que el rol del bot, no se puede agregar").queue();
                  return;
               }

               map.put(emoji, role.getAsMention());
            }

            manager.setId(success.getIdLong());
            manager.setToggled(true);
            manager.setEmoji(map);

            try {
               Server server = this.bot.getSettingsManager().getSettings(event.getGuild().getIdLong());
               server.addToRoleManagers(manager);
               server.persist();
            } catch (Exception var10) {
               event.reply(event.getClient().getError() + " Error al guardar el administrador de roles: " + var10.getMessage()).queue();
               return;
            }

            event.reply(event.getClient().getSuccess() + " Administrador de roles creado!").setEphemeral(true).queue();
         });
      }
   }

   public void execute(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.replyError(" Por favor incluya al menos un mensaje, un emoji y un rol");
      } else {
         String message = event.getArgs();
         String[] data = message.split("] ");
         String[] emojisnroles = data[1].split(" ");
         if (emojisnroles.length % 2 != 0) {
            event.replyError(" Por favor incluya correctamente los emojis y roles");
         } else if (emojisnroles.length < 1) {
            event.replyError(" Por favor incluya al menos un emoji y un rol");
         } else {
            message = data[0].substring(1);
            RoleManager manager = new RoleManager();
            EmbedBuilder eb = new EmbedBuilder();
            eb.setDescription(message);
            event.getTextChannel().sendMessageEmbeds(eb.build(), new MessageEmbed[0]).queue(success -> {
               HashMap<String, String> map = new HashMap<>();

               for (int i = 0; i < emojisnroles.length; i += 2) {
                  List<Role> roles = FinderUtil.findRoles(emojisnroles[i + 1].trim(), event.getGuild());
                  if (emojisnroles[i].startsWith("<")) {
                     emojisnroles[i] = emojisnroles[i].substring(2, emojisnroles[i].length() - 1);
                  }

                  success.addReaction(Emoji.fromFormatted(emojisnroles[i])).queue();
                  Role role = roles.get(0);
                  if (!event.getGuild().getSelfMember().canInteract(role)) {
                     success.delete().queue();
                     event.replyError("El rol " + role.getAsMention() + " es mayor que el rol del bot, no se puede agregar");
                     return;
                  }

                  map.put(emojisnroles[i], role.getAsMention());
               }

               manager.setId(success.getIdLong());
               manager.setEmoji(map);
               manager.setToggled(true);

               try {
                  Server server = this.bot.getSettingsManager().getSettings(event.getGuild().getIdLong());
                  server.addToRoleManagers(manager);
                  server.persist();
               } catch (Exception var9) {
                  event.replyError(" Error al guardar el administrador de roles: " + var9.getMessage());
                  return;
               }

               event.getMessage().delete().queue();
            });
         }
      }
   }
}











