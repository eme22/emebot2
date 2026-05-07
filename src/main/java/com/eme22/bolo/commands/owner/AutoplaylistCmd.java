package com.eme22.bolo.commands.owner;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.OwnerCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.Collections;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class AutoplaylistCmd extends OwnerCommand {
   private final Bot bot;
   @ConfigProperty(name = "config.aliases.autoplaylist", defaultValue = "")
   String[] aliases = new String[0];

   public AutoplaylistCmd(Bot bot) {
      this.bot = bot;
      this.guildOnly = true;
      this.name = "autoplaylist";
      this.arguments = "<name|NONE>";
      this.help = "sets the default playlist for the server";
      this.options = Collections.singletonList(new OptionData(OptionType.STRING, "playlist", "Selecciona una playlist predefinida").setRequired(true));
   }

   protected void execute(SlashCommandEvent event) {
      String playlist = (String)event.getOption("playlist", OptionMapping::getAsString);
      if (playlist == null) {
         event.reply(event.getClient().getError() + " No hay playlist seleccionada!!").queue();
      } else if (playlist.equalsIgnoreCase("none")) {
         Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
         settings.setDefaultPlaylist(null);
         settings.persist();
         event.reply(event.getClient().getSuccess() + " Cleared the default playlist for **" + event.getGuild().getName() + "**").queue();
      } else {
         String pname = playlist.replaceAll("\\s+", "_");
         if (this.bot.getPlaylistLoader().getPlaylist(pname) == null) {
            event.reply(event.getClient().getError() + " Could not find `" + pname + ".txt`!").queue();
         } else {
            Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
            settings.setDefaultPlaylist(pname);
            event.reply(event.getClient().getSuccess() + " The default playlist for **" + event.getGuild().getName() + "** is now `" + pname + "`").queue();
         }
      }
   }

   public void execute(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.reply(event.getClient().getError() + " Please include a playlist name or NONE");
      } else if (event.getArgs().equalsIgnoreCase("none")) {
         Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
         settings.setDefaultPlaylist(null);
         settings.persist();
         event.reply(event.getClient().getSuccess() + " Cleared the default playlist for **" + event.getGuild().getName() + "**");
      } else {
         String pname = event.getArgs().replaceAll("\\s+", "_");
         if (this.bot.getPlaylistLoader().getPlaylist(pname) == null) {
            event.reply(event.getClient().getError() + " Could not find `" + pname + ".txt`!");
         } else {
            Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
            settings.setDefaultPlaylist(pname);
            event.reply(event.getClient().getSuccess() + " The default playlist for **" + event.getGuild().getName() + "** is now `" + pname + "`");
         }
      }
   }
}


