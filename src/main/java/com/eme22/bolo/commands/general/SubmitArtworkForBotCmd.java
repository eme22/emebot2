package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class SubmitArtworkForBotCmd extends BaseCommand {
   private final long owner;

   public SubmitArtworkForBotCmd(Bot bot, @ConfigProperty(name = "config.aliases.submitartwork", defaultValue = "") String[] aliases, @ConfigProperty(name = "config.owner") long owner) {
      this.name = "submitartwork";
      this.help = "envia un artwork para que sea analizado y agregado al bot de musica";
      this.arguments = "<artista> <link>";
      this.options = Arrays.asList(
         new OptionData(OptionType.STRING, "artista", "artista a agregar.").setRequired(true),
         new OptionData(OptionType.STRING, "link", "link del gif o imagen").setRequired(true)
      );
      this.guildOnly = true;
      this.owner = owner;
      this.aliases = aliases;
   }

   public void execute(SlashCommandEvent event) {
      String artist = Objects.requireNonNull(event.getOption("artista")).getAsString();
      String link = Objects.requireNonNull(event.getOption("link")).getAsString();

      try {
         java.net.URI.create(link).toURL();
      } catch (java.net.MalformedURLException | IllegalArgumentException var5) {
         event.reply(event.getClient().getError() + " Link Incorrecto").setEphemeral(true).queue();
         return;
      }

      event.getJDA().getUserById(this.owner).openPrivateChannel().queue(privateChannel -> {
         this.sendArtworkConfirmation(artist, link, privateChannel);
         event.reply(event.getClient().getSuccess() + " Enviado!!!").setEphemeral(true).queue();
      });
   }

   private void sendArtworkConfirmation(String artist, String link, PrivateChannel privateChannel) {
      MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
      ArrayList<MessageEmbed> embeds = new ArrayList<>();
      EmbedBuilder eb = new EmbedBuilder();
      eb.setDescription(artist);
      embeds.add(eb.build());
      eb = new EmbedBuilder();
      eb.setDescription(link);
      eb.setImage(link);
      embeds.add(eb.build());
      messageCreateBuilder.setEmbeds(embeds);
      messageCreateBuilder.setComponents(ActionRow.of(Button.primary("acceptArtWork", "Aceptar"), Button.danger("rejectArtWork", "Reject")));
      privateChannel.sendMessage(messageCreateBuilder.build()).queue();
   }

   public void execute(CommandEvent event) {
      String link = null;
      if (event.getArgs().isEmpty()) {
         List<Attachment> attachmentList = event.getMessage().getAttachments();
         if (attachmentList.isEmpty()) {
            event.reply(event.getClient().getError() + " Incluya nombre y un link");
            return;
         }

         link = attachmentList.get(0).getUrl();
      }

      String message;
      if (link != null) {
         message = event.getArgs();
      } else {
         String args = event.getArgs();
         message = args.substring(0, args.lastIndexOf(" "));
         link = args.substring(args.lastIndexOf(" ") + 1);
      }

      try {
         java.net.URI.create(link).toURL();
      } catch (java.net.MalformedURLException | IllegalArgumentException var5) {
         event.replyError(" Link Incorrecto");
         return;
      }

      String finalLink = link;
      event.getJDA().getUserById(this.owner).openPrivateChannel().queue(privateChannel -> {
         this.sendArtworkConfirmation(message, finalLink, privateChannel);
         event.replySuccess(" Enviado!!!", message1 -> message1.delete().queueAfter(5L, TimeUnit.SECONDS));
      });
   }
}










