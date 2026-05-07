package com.eme22.bolo.commands.general.nsfw;

import com.eme22.bolo.commands.general.ActionsCmd;
import com.eme22.bolo.nsfw.NSFWStrings;
import com.eme22.bolo.stats.StatsService;
import com.eme22.imageapi.AnimeImageClient;
import com.eme22.imageapi.util.Endpoints.HM_NSFW;
import java.io.IOException;
import java.net.URISyntaxException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class AnalCmd extends ActionsCmd {
   @ConfigProperty(name = "config.aliases.anal", defaultValue = "")
   String[] aliases = new String[0];

   public AnalCmd(@ConfigProperty(name = "config.aliases.anal", defaultValue = "") String[] aliases, StatsService statsService, AnimeImageClient animeImageClient) {
      super("anal", aliases, statsService, animeImageClient);
      this.success = new ActionsCmd.Consumer<InteractionHook>() {
         public void accept(InteractionHook success) {
            super.accept(success);
            statsService.updateAnals(success.getInteraction().getGuild().getIdLong());
         }
      };
      this.success1 = new ActionsCmd.Consumer<Message>() {
         public void accept(Message success) {
            super.accept(success);
            statsService.updateAnals(success.getGuild().getIdLong());
         }
      };
   }

   @Override
   protected String getActionDescription() {
      return NSFWStrings.getRandomAnal();
   }

   @Override
   protected String loadActionImageUrl1(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getImage(HM_NSFW.ANAL);
   }

   @Override
   protected String loadActionImageUrl2(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getImage(HM_NSFW.ANAL);
   }

   @Override
   protected String loadActionImageUrl3(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getImage(HM_NSFW.ANAL);
   }
}


