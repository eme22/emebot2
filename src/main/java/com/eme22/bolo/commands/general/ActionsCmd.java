package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.stats.StatsService;
import com.eme22.imageapi.AnimeImageClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lombok.Generated;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Transactional
@ActivateRequestContext
public abstract class ActionsCmd extends BaseCommand {
   @Generated
   
   private static int maxRetries = 3;
   private int retries = 0;
   protected final StatsService statsService;
   protected ActionsCmd.Consumer<InteractionHook> success = new ActionsCmd.Consumer<>();
   protected ActionsCmd.Consumer<Message> success1 = new ActionsCmd.Consumer<>();
   protected final AnimeImageClient animeImageClient;

   public ActionsCmd(String name2, StatsService statsService, AnimeImageClient animeImageClient) {
      this.name = name2;
      this.help = name2 + " al usuario seleccionado";
      this.arguments = "<user>";
      this.guildOnly = true;
      this.statsService = statsService;
      this.options = Collections.singletonList(new OptionData(OptionType.USER, "usuario", "busca el usuario a " + name2 + ".").setRequired(true));
      this.animeImageClient = animeImageClient;
   }

   public ActionsCmd(String name2, String[] aliases, StatsService statsService, AnimeImageClient animeImageClient) {
      this.name = name2;
      this.help = name2 + " al usuario seleccionado";
      this.aliases = aliases;
      this.arguments = "<user>";
      this.guildOnly = true;
      this.statsService = statsService;
      this.options = Collections.singletonList(new OptionData(OptionType.USER, "usuario", "busca el usuario a " + name2 + ".").setRequired(true));
      this.animeImageClient = animeImageClient;
   }

   protected abstract String getActionDescription();

   protected abstract String loadActionImageUrl1(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException;

   protected abstract String loadActionImageUrl2(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException;

   protected abstract String loadActionImageUrl3(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException;

   public void execute(SlashCommandEvent event) {
      Member memberKisser = event.getMember();
      Member memberKissed = event.getOption("usuario").getAsMember();
      if (memberKissed.getUser().isBot()) {
         event.reply(event.getClient().getError() + " Asegurese de que el usuario no sea un bot").setEphemeral(true).queue();
      } else if (memberKisser.equals(memberKissed)) {
         event.reply(event.getClient().getError() + "Asegurese de que el usuario no sea usted").setEphemeral(true).queue();
      } else {
         EmbedBuilder builder = new EmbedBuilder();
         builder.setDescription(memberKisser.getAsMention() + this.getActionDescription() + memberKissed.getAsMention());
         builder.setImage(this.getRandomImage());
         event.replyEmbeds(builder.build(), new MessageEmbed[0]).queue(this.success);
      }

   }

   public void execute(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.replyError(" Debe nombrar a un usuario");
      } else {
         List<Member> list = FinderUtil.findMembers(event.getArgs(), event.getGuild());
         if (list.isEmpty()) {
            event.replyError(" No se ha podido encontrar a ese usuario");
         } else {
            Member memberKisser = event.getMember();
            Member memberKissed = (Member)list.get(0);
            if (memberKissed.getUser().isBot()) {
               event.replyError("Asegurese de que el usuario no sea un bot");
            } else if (memberKisser.equals(memberKissed)) {
               event.replyError("Asegurese de que el usuario no sea usted");
            } else {
               EmbedBuilder builder = new EmbedBuilder();
               builder.setDescription(memberKisser.getAsMention() + this.getActionDescription() + memberKissed.getAsMention());
               builder.setImage(this.getRandomImage());
               event.reply(builder.build(), this.success1);
            }
         }
      }
   }

   private String getRandomImage() {
      try {
         if (new Random().nextBoolean()) {
            return new Random().nextBoolean() ? this.loadActionImageUrl1(this.animeImageClient) : this.loadActionImageUrl3(this.animeImageClient);
         } else {
            return new Random().nextBoolean() ? this.loadActionImageUrl2(this.animeImageClient) : this.loadActionImageUrl3(this.animeImageClient);
         }
      } catch (Exception var2) {
         log.warn("No se ha podido obtener una imagen", var2);
         if (this.retries <= maxRetries) {
            this.retries++;
            return this.getRandomImage();
         } else {
            this.retries = 0;
            return null;
         }
      }
   }

   protected class Consumer<T> implements java.util.function.Consumer<T> {
      @Override
      public void accept(T success) {
         if (success instanceof Message) {
            long guildId = ((Message)success).getGuild().getIdLong();
            ActionsCmd.this.statsService.updateImagesSend(guildId);
            updateActionStat(guildId);
         } else if (success instanceof InteractionHook) {
            long guildId = ((InteractionHook)success).getInteraction().getGuild().getIdLong();
            ActionsCmd.this.statsService.updateImagesSend(guildId);
            updateActionStat(guildId);
         }
      }
   }

   protected void updateActionStat(long guildId) {
        switch(this.name.toLowerCase()) {
            case "kiss": this.statsService.updateKisses(guildId); break;
            case "slap": this.statsService.updateSlaps(guildId); break;
            case "poke": this.statsService.updatePokes(guildId); break;
            case "bite": this.statsService.updateBites(guildId); break;
            case "lick": this.statsService.updateLicks(guildId); break;
            case "fuck": this.statsService.updateFucks(guildId); break;
            case "cum": this.statsService.updateCums(guildId); break;
            case "anal": this.statsService.updateAnals(guildId); break;
        }
   }
}




