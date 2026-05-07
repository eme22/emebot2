package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import com.jagrosh.jdautilities.menu.Paginator.Builder;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class EightBallAnswerList extends AdminCommand {
   private final Builder builder;

   public EightBallAnswerList(Bot bot, @Named("adminCategory") Category category, @ConfigProperty(name = "config.aliases.8ballanswers", defaultValue = "") String[] aliases) {
      super(category);
      this.name = "8ballanswers";
      this.help = "muestra la lista de respuestas del comando 8ball del servidor";
      this.guildOnly = true;
      this.aliases = aliases;
      this.builder = (Builder)((Builder)new Builder().setColumns(1).setFinalAction(m -> {
            try {
               m.clearReactions().queue();
            } catch (PermissionException var2) {
            }
         }).setItemsPerPage(20).waitOnSinglePage(false).useNumberedItems(true).showPageNumbers(true).wrapPageEnds(true).setEventWaiter(bot.getWaiter()))
         .setTimeout(10L, TimeUnit.MINUTES);
   }

   protected void execute(SlashCommandEvent event) {
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      List<String> data = s.getEightBallAnswers();
      if (data.isEmpty()) {
         event.reply(event.getClient().getError() + " No hay respuestas para mostrar").setEphemeral(true).queue();
      } else {
         event.reply(event.getClient().getSuccess() + " Lista de respuestas del comando 8ball").queue();
         this.builder.setText("").setItems(data.toArray(new String[0]));
         this.builder.build().paginate(event.getChannel(), 1);
      }
   }

   protected void execute(CommandEvent event) {
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      List<String> data = s.getEightBallAnswers();
      if (data.isEmpty()) {
         event.replyError(" No hay respuestas para mostrar");
      } else {
         event.replySuccess(" Lista de respuestas del comando 8ball");
         this.builder.setText("").setItems(data.toArray(new String[0]));
         this.builder.build().paginate(event.getChannel(), 1);
      }
   }
}



