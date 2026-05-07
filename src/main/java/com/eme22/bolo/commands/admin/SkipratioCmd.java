package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.Collections;
import java.util.Objects;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class SkipratioCmd extends AdminCommand {
   @ConfigProperty(name = "config.aliases.setskip", defaultValue = "")
   String[] aliases = new String[0];

   public SkipratioCmd(@Named("adminCategory") Category category) {
      super(category);
      this.name = "setskip";
      this.help = "pone un radio para el comando skip";
      this.arguments = "<0 - 100>";
      this.options = Collections.singletonList(
         new OptionData(OptionType.INTEGER, "radio", "porcentaje de aprobacion para comando voteskip").setMinValue(0L).setMaxValue(100L).setRequired(true)
      );
   }

   protected void execute(SlashCommandEvent event) {
      int val = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(event.getOption("radio")).getAsString()));
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      s.setSkipRatio(val / 100.0);
      s.persist();
      event.reply(event.getClient().getSuccess() + " Skip percentage has been set to `" + val + "%` of listeners on *" + event.getGuild().getName() + "*")
         .queue();
   }

   protected void execute(CommandEvent event) {
      try {
         int val = Integer.parseInt(event.getArgs().endsWith("%") ? event.getArgs().substring(0, event.getArgs().length() - 1) : event.getArgs());
         if (val < 0 || val > 100) {
            event.replyError("The provided value must be between 0 and 100!");
            return;
         }

         Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
         s.setSkipRatio(val / 100.0);
         s.persist();
         event.replySuccess("Skip percentage has been set to `" + val + "%` of listeners on *" + event.getGuild().getName() + "*");
      } catch (NumberFormatException var4) {
         event.replyError(
            "Please include an integer between 0 and 100 (default is 55). This number is the percentage of listening users that must vote to skip a song."
         );
      }
   }
}



