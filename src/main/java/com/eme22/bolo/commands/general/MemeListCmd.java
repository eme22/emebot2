package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.model.MemeImage;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.menu.Paginator.Builder;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class MemeListCmd extends BaseCommand {
   private final Builder builder;
   @ConfigProperty(name = "config.aliases.memelist", defaultValue = "")
   String[] aliases = new String[0];

   public MemeListCmd(Bot bot) {
      this.name = "memelist";
      this.help = "muestra la lista de memes del servidor";
      this.guildOnly = true;
      this.builder = (Builder)((Builder)new Builder().setColumns(1).setFinalAction(m -> {
            try {
               m.clearReactions().queue();
            } catch (PermissionException var2) {
            }
         }).setItemsPerPage(20).waitOnSinglePage(false).useNumberedItems(true).showPageNumbers(true).wrapPageEnds(true).setEventWaiter(bot.getWaiter()))
         .setTimeout(10L, TimeUnit.MINUTES);
   }

   public void execute(SlashCommandEvent event) {
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      List<MemeImage> data = s.getMemeImages();
      String[] songs = new String[data.size()];

      for (int i = 0; i < data.size(); i++) {
         songs[i] = data.get(i).getMessage();
      }

      if (songs.length == 0) {
         event.reply(event.getClient().getError() + "No hay memes para mostrar").setEphemeral(true).queue();
      } else {
         event.reply(event.getClient().getSuccess() + " Lista de memes").queue();
         this.builder.setText("").setItems(songs);
         this.builder.build().paginate(event.getChannel(), 1);
      }
   }

   public void execute(CommandEvent event) {
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      List<MemeImage> data = s.getMemeImages();
      String[] songs = new String[data.size()];

      for (int i = 0; i < data.size(); i++) {
         songs[i] = data.get(i).getMessage();
      }

      if (songs.length == 0) {
         event.replyError(" No hay memes para mostrar");
      } else {
         this.builder.setText(event.getClient().getSuccess() + " Lista de memes").setItems(songs);
         this.builder.build().paginate(event.getChannel(), 1);
      }
   }
}










