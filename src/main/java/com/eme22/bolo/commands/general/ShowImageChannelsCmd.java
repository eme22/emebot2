package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class ShowImageChannelsCmd extends BaseCommand {
   @ConfigProperty(name = "config.aliases.showimgch", defaultValue = "")
   String[] aliases = new String[0];

   public ShowImageChannelsCmd(Bot bot) {
      this.name = "showimgch";
      this.help = "muestra los canales de solo imagen listados en el servidor";
      this.guildOnly = true;
   }

   protected void execute(SlashCommandEvent event) {
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      List<Long> onlyimages = s.getImageOnlyChannelsIds();
      StringBuilder builder1 = new StringBuilder();
      onlyimages.forEach(image -> builder1.append(event.getGuild().getTextChannelById(image).getName()).append(" \n"));
      EmbedBuilder ebuilder = new EmbedBuilder().setColor(event.getGuild().getSelfMember().getColor()).setDescription(builder1.toString());
      MessageCreateBuilder mbuilder = new MessageCreateBuilder();
      mbuilder.setContent(" ** Canales de solo Imagenes **");
      mbuilder.setEmbeds(new MessageEmbed[]{ebuilder.build()});
      event.reply(mbuilder.build()).queue();
   }

   protected void execute(CommandEvent event) {
      Server s = (Server)event.getClient().getSettingsFor(event.getGuild());
      List<Long> onlyimages = s.getImageOnlyChannelsIds();
      StringBuilder builder1 = new StringBuilder();
      onlyimages.forEach(image -> builder1.append(event.getGuild().getTextChannelById(image).getName()).append(" \n"));
      EmbedBuilder ebuilder = new EmbedBuilder().setColor(event.getSelfMember().getColor()).setDescription(builder1.toString());
      MessageCreateBuilder mbuilder = new MessageCreateBuilder();
      mbuilder.setContent(" ** Canales de solo Imagenes **");
      mbuilder.setEmbeds(new MessageEmbed[]{ebuilder.build()});
      event.reply(mbuilder.build());
   }
}


