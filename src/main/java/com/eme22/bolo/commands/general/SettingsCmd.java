package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.model.RepeatMode;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.utils.FormatUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;
@Singleton
@Transactional
@ActivateRequestContext
public class SettingsCmd extends BaseCommand {
   private static final String EMOJI = "\ud83c\udfa7";
   @ConfigProperty(name = "config.aliases.settings", defaultValue = "")
   String[] aliases = new String[0];
   Bot bot;

   public SettingsCmd(Bot bot) {
      this.name = "settings";
      this.help = "muestra las opciones del bot";
      this.guildOnly = true;
      this.bot = bot;
   }

   @Override
   public void execute(SlashCommandEvent event) {
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      String builder = "\ud83c\udfa7 **" + FormatUtil.filter(event.getGuild().getSelfMember().getEffectiveName()) + "** settings:";
      TextChannel wchan = event.getGuild().getTextChannelById(s.getBienvenidasChannelId());
      TextChannel dchan = event.getGuild().getTextChannelById(s.getDespedidasChannelId());
      TextChannel tchan = event.getGuild().getTextChannelById(s.getTextChannelId());
      VoiceChannel vchan = event.getGuild().getVoiceChannelById(s.getVoiceChannelId());
      Role djRole = event.getGuild().getRoleById(s.getDjRoleId());
      Role adminrole = event.getGuild().getRoleById(s.getAdminRoleId());
      List<Long> onlyimages = s.getImageOnlyChannelsIds();
      EmbedBuilder ebuilder = new EmbedBuilder()
         .setColor(event.getGuild().getSelfMember().getColor())
         .setDescription(
            "Canal de Musica: "
               + (tchan == null ? "Cualquiera" : "**#" + tchan.getName() + "**")
               + "\nCanal de Bienvenida: "
               + (wchan == null ? "Cualquiera" : "**#" + wchan.getName() + "**")
               + "\nCanal de Despedidas: "
               + (dchan == null ? "Cualquiera" : "**#" + dchan.getName() + "**")
               + "\nCanal de Voz: "
               + (vchan == null ? "Cualquiera" : vchan.getAsMention())
               + "\nRol de Admin: "
               + (adminrole == null ? "Ninguno" : "**" + adminrole.getName() + "**")
               + "\nRol de DJ: "
               + (djRole == null ? "Ninguno" : "**" + djRole.getName() + "**")
               + "\nCanales de solo imagenes: "
               + (onlyimages.isEmpty() ? "Ninguno" : "**" + onlyimages.size() + "**")
               + "\nPrefijo Personalizado: "
               + (s.getPrefix() == null ? "Ninguno" : "`" + s.getPrefix() + "`")
               + "\nModo de Repeticion: "
               + (s.getRepeatMode() == RepeatMode.OFF ? s.getRepeatMode().getUserFriendlyName() : "**" + s.getRepeatMode().getUserFriendlyName() + "**")
               + "\nPlaylist por defecto: "
               + (s.getDefaultPlaylist() == null ? "Ninguno" : "**" + s.getDefaultPlaylist() + "**")
         )
         .setFooter(
            event.getJDA().getGuilds().size()
               + " servers | "
               + event.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inAudioChannel()).count()
               + " conecciones de audio",
            null
         );
      MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
      messageCreateBuilder.addContent(builder).setEmbeds(new MessageEmbed[]{ebuilder.build()});
      event.reply(messageCreateBuilder.build()).queue();
   }

   @Override
   public void execute(CommandEvent event) {
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      String builder = "\ud83c\udfa7 **" + FormatUtil.filter(event.getSelfMember().getEffectiveName()) + "** settings:";
      TextChannel wchan = event.getGuild().getTextChannelById(s.getBienvenidasChannelId());
      TextChannel dchan = event.getGuild().getTextChannelById(s.getDespedidasChannelId());
      TextChannel tchan = event.getGuild().getTextChannelById(s.getTextChannelId());
      VoiceChannel vchan = event.getGuild().getVoiceChannelById(s.getVoiceChannelId());
      Role djRole = event.getGuild().getRoleById(s.getDjRoleId());
      Role adminrole = event.getGuild().getRoleById(s.getAdminRoleId());
      List<Long> onlyimages = s.getImageOnlyChannelsIds();
      EmbedBuilder ebuilder = new EmbedBuilder()
         .setColor(event.getSelfMember().getColor())
         .setDescription(
            "Canal de Musica: "
               + (tchan == null ? "Cualquiera" : "**#" + tchan.getName() + "**")
               + "\nCanal de Bienvenida: "
               + (wchan == null ? "Cualquiera" : "**#" + wchan.getName() + "**")
               + "\nCanal de Despedidas: "
               + (dchan == null ? "Cualquiera" : "**#" + dchan.getName() + "**")
               + "\nCanal de Voz: "
               + (vchan == null ? "Cualquiera" : vchan.getAsMention())
               + "\nRol de Admin: "
               + (adminrole == null ? "Ninguno" : "**" + adminrole.getName() + "**")
               + "\nRol de DJ: "
               + (djRole == null ? "Ninguno" : "**" + djRole.getName() + "**")
               + "\nCanales de solo imagenes: "
               + (onlyimages.isEmpty() ? "Ninguno" : "**" + onlyimages.size() + "**")
               + "\nPrefijo Personalizado: "
               + (s.getPrefix() == null ? "Ninguno" : "`" + s.getPrefix() + "`")
               + "\nModo de Repeticion: "
               + (s.getRepeatMode() == RepeatMode.OFF ? s.getRepeatMode().getUserFriendlyName() : "**" + s.getRepeatMode().getUserFriendlyName() + "**")
               + "\nPlaylist por defecto: "
               + (s.getDefaultPlaylist() == null ? "Ninguno" : "**" + s.getDefaultPlaylist() + "**")
         )
         .setFooter(
            event.getJDA().getGuilds().size()
               + " servers | "
               + event.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inAudioChannel()).count()
               + " conecciones de audio",
            null
         );
      MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
      messageCreateBuilder.addContent(builder).setEmbeds(new MessageEmbed[]{ebuilder.build()});
      event.reply(messageCreateBuilder.build());
   }
}







