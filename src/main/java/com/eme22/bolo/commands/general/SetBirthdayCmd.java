package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.model.Birthday;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class SetBirthdayCmd extends BaseCommand {
   Bot bot;
   @ConfigProperty(name = "config.aliases.setbirthday", defaultValue = "")
   String[] aliases = new String[0];

   public SetBirthdayCmd(Bot bot) {
      this.bot = bot;
      this.name = "setbirthday";
      this.help = "agrega tu cumpleaÃ±os al servidor: <dia> <mes> <mensaje (@me para mencionarte)>";
      this.guildOnly = true;
      this.options = Arrays.asList(
         new OptionData(OptionType.INTEGER, "dia", "dia de tu cumpleaÃ±os").setRequired(true),
         new OptionData(OptionType.INTEGER, "mes", "mes de tu cumpleaÃ±os").setRequired(true),
         new OptionData(OptionType.STRING, "mensaje", "mensaje el dia tu cumpleaÃ±os - comandos especiales @me para mencionarte").setRequired(true)
      );
   }

   public void execute(SlashCommandEvent event) {
      try {
         int dia = Integer.parseInt(event.getOption("dia").getAsString());
         int mes = Integer.parseInt(event.getOption("mes").getAsString());
         String mensaje = event.getOption("mensaje").getAsString();
         mensaje = mensaje.replaceAll("@me", event.getMember().getAsMention());
         int year = Calendar.getInstance().get(1);
         Server settings = this.bot.getSettingsManager().getSettings(event.getGuild());
         Birthday cumple = new Birthday();
         cumple.setDate(LocalDate.of(year, mes, dia));
         cumple.setUser(event.getUser().getIdLong());
         cumple.setMessage(mensaje);
         cumple.setEnabled(true);
         cumple.setServer(event.getGuild().getIdLong());
         Birthday old = settings.getUserBirthday(event.getMember().getUser().getIdLong());
         if (old != null) {
            settings.removeBirthDay(event.getMember().getUser().getIdLong());
            settings.persist();
         }

         settings.addBirthDay(cumple);
         settings.persist();
         event.reply(event.getClient().getSuccess() + "Se recordarÃ¡ tu cumpleaÃ±os el " + dia + "/" + mes).setEphemeral(true).queue();
      } catch (NumberFormatException var9) {
         event.reply(event.getClient().getError() + " La fecha no es valida").setEphemeral(true).queue();
      }
   }

   public void execute(CommandEvent event) {
      try {
         String[] args = event.getArgs().split(" ");
         Server settings = this.bot.getSettingsManager().getSettings(event.getGuild());
         int dia = Integer.parseInt(args[0]);
         int mes = Integer.parseInt(args[1]);
         String message = event.getArgs().substring(event.getArgs().indexOf(" ", 2) + 1);
         message = message.replaceAll("@me", event.getAuthor().getAsMention());
         int year = Calendar.getInstance().get(1);
         Birthday cumple = new Birthday();
         cumple.setDate(LocalDate.of(year, mes, dia));
         cumple.setUser(event.getMember().getUser().getIdLong());
         cumple.setServer(event.getGuild().getIdLong());
         cumple.setEnabled(true);
         cumple.setMessage(message);
         Birthday old = settings.getUserBirthday(event.getMember().getUser().getIdLong());
         if (old != null) {
            settings.removeBirthDay(event.getMember().getUser().getIdLong());
            settings.persist();
         }

         settings.addBirthDay(cumple);
         settings.persist();
         event.replySuccess(event.getClient().getSuccess() + "Se recordarÃ¡ tu cumpleaÃ±os el " + dia + "/" + mes);
      } catch (NumberFormatException var10) {
         event.replyError(" La fecha no es valida");
      }
   }
}










