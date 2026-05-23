package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.utils.MemeUtil;
import com.eme22.bolo.utils.OtherUtil;
import com.eme22.bolo.stats.StatsService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import lombok.Generated;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;

import org.jetbrains.annotations.NotNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Slf4j
@Singleton
@Transactional
@ActivateRequestContext
public class CreateMemeCmd extends BaseCommand {
   @Generated
   
   @ConfigProperty(name = "config.clientToken", defaultValue = "")
   String clientToken;
   private final StatsService statsService;
   private MemeUtil memeUtil;

   @Inject
   public CreateMemeCmd(StatsService statsService, @ConfigProperty(name = "config.aliases.creatememe", defaultValue = "") String[] aliases) {
      this.name = "creatememe";
      this.arguments = "<URL IMAGEN> [TEXTO SUPERIOR] [TEXTO INFERIOR]";
      this.help = "genera un meme desde una imagen";
      this.guildOnly = true;
      this.statsService = statsService;
      this.aliases = aliases;
      this.options = Arrays.asList(
         new OptionData(OptionType.STRING, "url", "url de la imagen base.").setRequired(true),
         new OptionData(OptionType.STRING, "superior", "texto superior del meme.").setRequired(true),
         new OptionData(OptionType.STRING, "inferior", "texto inferior del meme.").setRequired(true)
      );
   }

   public void execute(SlashCommandEvent event) {
      String url = event.getOption("url").getAsString();
      String textoS = event.getOption("superior").getAsString();
      String textoI = event.getOption("inferior").getAsString();
      File image = this.generateFile();

      try {
         InputStream str = OtherUtil.imageFromUrl(url, this.clientToken);
         this.memeUtil = new MemeUtil(ImageIO.read(str));
         str.close();
      } catch (IOException var8) {
         log.error("Error al generar el meme", var8);
         event.reply(event.getClient().getError() + " se ha producido un error al generar el meme").queue();
      }

      try {
         ImageIO.write(this.memeUtil.generateMeme(textoS, textoI), "png", image);
      } catch (IOException var7) {
         log.error("Error al generar el meme", var7);
         event.reply(event.getClient().getError() + " se ha producido un error al generar el meme").queue();
      }

      EmbedBuilder eb = new EmbedBuilder();
      eb.setImage("attachment://tempMeme.png");
      ((ReplyCallbackAction)event.replyEmbeds(eb.build(), new MessageEmbed[0]).addFiles(new FileUpload[]{FileUpload.fromData(image)}))
         .queue(end -> {
            this.statsService.increment(event.getGuild().getIdLong(), "IMAGES_SEND");
            this.statsService.increment(event.getGuild().getIdLong(), "MEMES_SEND");
            image.delete();
         });
   }

   public void execute(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.replyError(" Por favor incluya al menos un usuario y mensaje");
      } else {
         String regexGeneral = "<(.*?)> \\[(.*?)\\] \\[(.*?)\\]";
         Pattern pattern = Pattern.compile(regexGeneral, 2);
         Matcher matcher = pattern.matcher(event.getArgs());
         boolean matchFound = matcher.find();
         if (matchFound) {
            String url = matcher.group(1);
            String textoS = matcher.group(2);
            String textoI = matcher.group(3);
            File image = this.generateFile();
            System.out.println(url);

            try {
               InputStream str = OtherUtil.imageFromUrl(url, this.clientToken);
               this.memeUtil = new MemeUtil(ImageIO.read(str));
               str.close();
            } catch (IOException var12) {
               log.error("Error al generar el meme", var12);
               event.replyError(" se ha producido un error al generar el meme");
            }

            try {
               ImageIO.write(this.memeUtil.generateMeme(textoS, textoI), "png", image);
            } catch (IOException var11) {
               log.error("Error al generar el meme", var11);
               event.replyError(" se ha producido un error al generar el meme");
            }

            System.out.println(image);
            EmbedBuilder eb = new EmbedBuilder();
            eb.setImage("attachment://tempMeme.png");
            ((MessageCreateAction)event.getChannel().sendMessageEmbeds(eb.build(), new MessageEmbed[0]).addFiles(new FileUpload[]{FileUpload.fromData(image)}))
               .queue(end -> {
                  this.statsService.increment(event.getGuild().getIdLong(), "IMAGES_SEND");
                  this.statsService.increment(event.getGuild().getIdLong(), "MEMES_SEND");
                  image.delete();
               });
         } else {
            event.replyError(" Por favor incluya al menos un usuario y mensaje");
         }
      }
   }

   @NotNull
   private File generateFile() {
      File parent = new File("temp");
      if (!parent.exists() && parent.mkdirs()) {
         log.error("Temp folder successfully created");
      }

      File converted = new File(parent, "tempMeme.png");
      if (converted.delete()) {
         log.error("Image deleted from memory before new image");
      }

      return converted;
   }
}










