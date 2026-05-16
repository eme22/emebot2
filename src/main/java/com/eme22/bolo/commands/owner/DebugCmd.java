package com.eme22.bolo.commands.owner;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.utils.FileUpload;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class DebugCmd extends OwnerCommand {
   private static final String[] PROPERTIES = new String[]{
      "java.version",
      "java.vm.name",
      "java.vm.specification.version",
      "java.runtime.name",
      "java.runtime.version",
      "java.specification.version",
      "os.arch",
      "os.name"
   };
   @ConfigProperty(name = "config.aliases.debug", defaultValue = "")
   String[] aliases = new String[0];
   @ConfigProperty(name = "config.owner")
   private long owner;
   @ConfigProperty(name = "config.prefix")
   private String prefix;
   @ConfigProperty(name = "config.altprefix")
   private String altprefix;
   @ConfigProperty(name = "config.maxseconds")
   private long maxSeconds;
   @ConfigProperty(name = "config.nowplayingimages")
   private boolean npImages;
   @ConfigProperty(name = "config.stayinchannel")
   private boolean stayInChannel;
   @ConfigProperty(name = "config.songinstatus")
   private boolean songInStatus;
   @ConfigProperty(name = "config.eval")
   private boolean useEval;
   @ConfigProperty(name = "config.update")
   private boolean updatealerts;
   private final Bot bot;
   private final String version;

   public DebugCmd(Bot bot, @ConfigProperty(name = "quarkus.application.version", defaultValue = "0.0.1") String version) {
      this.bot = bot;
      this.version = version;
      this.name = "debug";
      this.help = "shows debug info";
      this.guildOnly = false;
   }

   public void execute(CommandEvent event) {
      String sb = this.getDebugMessage(event.getJDA());
      if (!event.isFromType(ChannelType.PRIVATE)
         && !event.getSelfMember().hasPermission(event.getTextChannel(), new Permission[]{Permission.MESSAGE_ATTACH_FILES})) {
         event.reply("Debug Information: " + sb);
      } else {
         event.getChannel().sendFiles(new FileUpload[]{FileUpload.fromData(sb.getBytes(), "debug_information.txt")}).queue();
      }
   }

   public void execute(SlashCommandEvent event) {
      event.reply("Debug Information: " + this.getDebugMessage(event.getJDA())).queue();
   }

   private String getDebugMessage(JDA jda) {
      StringBuilder sb = new StringBuilder();
      sb.append("```\nSystem Properties:");

      for (String key : PROPERTIES) {
         sb.append("\n  ").append(key).append(" = ").append(System.getProperty(key));
      }

      sb.append("\n\nJMusicBot Information:")
         .append("\n  Version = ")
         .append(this.version)
         .append("\n  Owner = ")
         .append(this.owner)
         .append("\n  Prefix = ")
         .append(this.prefix)
         .append("\n  AltPrefix = ")
         .append(this.altprefix)
         .append("\n  MaxSeconds = ")
         .append(this.maxSeconds)
         .append("\n  NPImages = ")
         .append(this.npImages)
         .append("\n  SongInStatus = ")
         .append(this.songInStatus)
         .append("\n  StayInChannel = ")
         .append(this.stayInChannel)
         .append("\n  UseEval = ")
         .append(this.useEval)
         .append("\n  UpdateAlerts = ")
         .append(this.updatealerts);
      sb.append("\n\nDependency Information:")
         .append("\n  JDA Version = ")
         .append(JDAInfo.VERSION)
         .append("\n  JDA-Utilities Version = ")
         .append(JDAUtilitiesInfo.VERSION)
         .append("\n  Lavalink-Client Version = ")
         .append("3.0.0");
      long total = Runtime.getRuntime().totalMemory() / 1024L / 1024L;
      long used = total - Runtime.getRuntime().freeMemory() / 1024L / 1024L;
      sb.append("\n\nRuntime Information:").append("\n  Total Memory = ").append(total).append("\n  Used Memory = ").append(used);
      sb.append("\n\nDiscord Information:")
         .append("\n  ID = ")
         .append(jda.getSelfUser().getId())
         .append("\n  Guilds = ")
         .append(jda.getGuildCache().size())
         .append("\n  Users = ")
         .append(jda.getUserCache().size());
      sb.append("\n```");
      return sb.toString();
   }
}











