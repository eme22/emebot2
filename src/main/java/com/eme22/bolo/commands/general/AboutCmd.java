package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.configuration.BotConfiguration;
import com.eme22.bolo.utils.Constants;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import lombok.Generated;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.JDA.ShardInfo;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@Slf4j
@Transactional
@ActivateRequestContext
public class AboutCmd extends BaseCommand {
   @Generated
   
   private boolean IS_AUTHOR = true;
   private String REPLACEMENT_ICON = "+";
   private final int color = 0x00CCCC;
   private final String description;
   private final Permission[] perms;
   private static String oauthLink;
   private final String[] features;

   @Inject
   public AboutCmd(@ConfigProperty(name = "quarkus.application.version", defaultValue = "0.0.1") String version) {
      this.description = String.format("Hola soy Sentinel' un BOT con lag) (v%s) ", version);
      this.features = Constants.features;
      this.name = "about";
      this.help = "muestra info sobre el bot";
      this.guildOnly = false;
      this.perms = BotConfiguration.RECOMMENDED_PERMS;
      this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
   }

   public void execute(SlashCommandEvent event) {
      if (oauthLink == null) {
         this.getOauthLink(event.getJDA());
      }

      EmbedBuilder builder = new EmbedBuilder();
      builder.setColor(event.isFromType(ChannelType.TEXT) ? event.getGuild().getSelfMember().getColorRaw() : this.color);
      builder.setAuthor("Informacion de " + event.getJDA().getSelfUser().getName() + "!", null, event.getJDA().getSelfUser().getAvatarUrl());
      boolean join = event.getClient().getServerInvite() != null && !event.getClient().getServerInvite().isEmpty();
      boolean inv = !oauthLink.isEmpty();
      String invline = "\n"
         + (join ? "Unete a mi servidor [`link`](" + event.getClient().getServerInvite() + ")" : (inv ? " " : ""))
         + (inv ? (join ? ", o " : "") + "[`invitame`](" + oauthLink + ") a tu servidor" : "")
         + "!";
      String author = event.getJDA().getUserById(event.getClient().getOwnerId()) == null
         ? "<@" + event.getClient().getOwnerId() + ">"
         : event.getJDA().getUserById(event.getClient().getOwnerId()).getName();
      StringBuilder descr = new StringBuilder()
         .append("Hola soy **")
         .append(event.getJDA().getSelfUser().getName())
         .append("**, ")
         .append(this.description)
         .append("\nI ")
         .append(this.IS_AUTHOR ? "fui escrito en java" : "mi creador/dueÃ±o")
         .append(" es **")
         .append(author)
         .append("** uso [ChewUtils](https://github.com/Chew/JDA-Chewtils) (")
         .append(JDAUtilitiesInfo.VERSION)
         .append(") en conjunto de la libreria [JDA](https://github.com/DV8FromTheWorld/JDA) (")
         .append(JDAInfo.VERSION)
         .append(")\nEscribe `")
         .append(event.getClient().getTextualPrefix())
         .append(event.getClient().getHelpWord())
         .append("` para ver mis comandos!")
         .append(!join && !inv ? "" : invline)
         .append("\n\nAlgunas de mis caracteristicas son : ```css");

      for (String feature : this.features) {
         descr.append("\n")
            .append(event.getClient().getSuccess().startsWith("<") ? this.REPLACEMENT_ICON : event.getClient().getSuccess())
            .append(" ")
            .append(feature);
      }

      descr.append(" ```");
      builder.setDescription(descr);
      if (event.getJDA().getShardInfo() == ShardInfo.SINGLE) {
         builder.addField("Estadisticas", event.getJDA().getGuilds().size() + " Servidores\n1 Nodo", true);
         builder.addField(
            "Usuarios",
            event.getJDA().getUsers().size() + " \n" + event.getJDA().getGuilds().stream().mapToInt(g -> g.getMembers().size()).sum() + " total",
            true
         );
         builder.addField("Canales", event.getJDA().getTextChannels().size() + " Texto\n" + event.getJDA().getVoiceChannels().size() + " Voz", true);
      } else {
         builder.addField(
            "Estadisticas",
            event.getClient().getTotalGuilds()
               + " Servidores\nNodo "
               + (event.getJDA().getShardInfo().getShardId() + 1)
               + "/"
               + event.getJDA().getShardInfo().getShardTotal(),
            true
         );
         builder.addField("Este nodo: ", event.getJDA().getUsers().size() + " Usuarios\n" + event.getJDA().getGuilds().size() + " Servidores", true);
         builder.addField(
            "", event.getJDA().getTextChannels().size() + " Canales de Texto\n" + event.getJDA().getVoiceChannels().size() + " Canales de Voz", true
         );
      }

      builder.setFooter("Ultimo Reinicio", (String)null);
      builder.setTimestamp(event.getClient().getStartTime());
      event.replyEmbeds(builder.build(), new MessageEmbed[0]).queue();
   }

   private void getOauthLink(JDA event) {
      try {
         ApplicationInfo info = (ApplicationInfo)event.retrieveApplicationInfo().complete();
         oauthLink = info.isBotPublic() ? info.getInviteUrl(0L, this.perms) : "";
      } catch (Exception var3) {
         log.warn("Could not generate invite link ", var3);
         oauthLink = "";
      }
   }

   public void execute(CommandEvent event) {
      if (oauthLink == null) {
         this.getOauthLink(event.getJDA());
      }

      EmbedBuilder builder = new EmbedBuilder();
      builder.setColor(event.isFromType(ChannelType.TEXT) ? event.getGuild().getSelfMember().getColorRaw() : this.color);
      builder.setAuthor("Informacion de " + event.getSelfUser().getName() + "!", null, event.getSelfUser().getAvatarUrl());
      boolean join = event.getClient().getServerInvite() != null && !event.getClient().getServerInvite().isEmpty();
      boolean inv = !oauthLink.isEmpty();
      String invline = "\n"
         + (join ? "Unete a mi servidor [`link`](" + event.getClient().getServerInvite() + ")" : (inv ? " " : ""))
         + (inv ? (join ? ", o " : "") + "[`invitame`](" + oauthLink + ") a tu servidor" : "")
         + "!";
      String author = event.getJDA().getUserById(event.getClient().getOwnerId()) == null
         ? "<@" + event.getClient().getOwnerId() + ">"
         : event.getJDA().getUserById(event.getClient().getOwnerId()).getName();
      StringBuilder descr = new StringBuilder()
         .append("Hola soy **")
         .append(event.getSelfUser().getName())
         .append("**, ")
         .append(this.description)
         .append("\nI ")
         .append(this.IS_AUTHOR ? "fui escrito en java" : "mi creador/dueÃ±o")
         .append(" es **")
         .append(author)
         .append("** uso [ChewUtils](https://github.com/Chew/JDA-Chewtils) (")
         .append(JDAUtilitiesInfo.VERSION)
         .append(") en conjunto de la libreria [JDA](https://github.com/DV8FromTheWorld/JDA) (")
         .append(JDAInfo.VERSION)
         .append(")\nEscribe `")
         .append(event.getClient().getTextualPrefix())
         .append(event.getClient().getHelpWord())
         .append("` para ver mis comandos!")
         .append(!join && !inv ? "" : invline)
         .append("\n\nAlgunas de mis caracteristicas son : ```css");

      for (String feature : this.features) {
         descr.append("\n")
            .append(event.getClient().getSuccess().startsWith("<") ? this.REPLACEMENT_ICON : event.getClient().getSuccess())
            .append(" ")
            .append(feature);
      }

      descr.append(" ```");
      builder.setDescription(descr);
      if (event.getJDA().getShardInfo() == ShardInfo.SINGLE) {
         builder.addField("Estadisticas", event.getJDA().getGuilds().size() + " Servidores\n1 Nodo", true);
         builder.addField(
            "Usuarios",
            event.getJDA().getUsers().size() + " \n" + event.getJDA().getGuilds().stream().mapToInt(g -> g.getMembers().size()).sum() + " total",
            true
         );
         builder.addField("Canales", event.getJDA().getTextChannels().size() + " Texto\n" + event.getJDA().getVoiceChannels().size() + " Voz", true);
      } else {
         builder.addField(
            "Estadisticas",
            event.getClient().getTotalGuilds()
               + " Servidores\nNodo "
               + (event.getJDA().getShardInfo().getShardId() + 1)
               + "/"
               + event.getJDA().getShardInfo().getShardTotal(),
            true
         );
         builder.addField("Este nodo: ", event.getJDA().getUsers().size() + " Usuarios\n" + event.getJDA().getGuilds().size() + " Servidores", true);
         builder.addField(
            "", event.getJDA().getTextChannels().size() + " Canales de Texto\n" + event.getJDA().getVoiceChannels().size() + " Canales de Voz", true
         );
      }

      builder.setFooter("Ultimo Reinicio", (String)null);
      builder.setTimestamp(event.getClient().getStartTime());
      event.reply(builder.build());
   }
}












