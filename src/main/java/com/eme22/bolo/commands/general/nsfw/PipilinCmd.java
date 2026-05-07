package com.eme22.bolo.commands.general.nsfw;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class PipilinCmd extends Command {
   public PipilinCmd() {
      this.name = "pipilin";
      this.help = "OwO";
      this.nsfwOnly = true;
   }

   protected void execute(CommandEvent event) {
      String[] images = new String[]{
         "https://media.discordapp.net/attachments/948076380040605726/966047815434399804/unknown.png",
         "https://media.discordapp.net/attachments/948076380040605726/966047829833433108/unknown.png"
      };
      String pickRandomImage = images[(int)(Math.random() * images.length)];
      EmbedBuilder response = new EmbedBuilder().setImage(pickRandomImage);
      event.getChannel().sendMessageEmbeds(response.build(), new MessageEmbed[0]).queue();
      event.getMessage().delete().queue();
   }
}
