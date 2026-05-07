package com.eme22.bolo.commands.general.nsfw;

import com.eme22.bolo.commands.general.ActionsCmd;
import com.eme22.bolo.nsfw.NSFWStrings;
import com.eme22.bolo.stats.StatsService;
import com.eme22.imageapi.AnimeImageClient;
import com.eme22.imageapi.util.Endpoints.HM_NSFW;
import java.io.IOException;
import java.net.URISyntaxException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class CumCmd extends ActionsCmd {
   public CumCmd(@ConfigProperty(name = "config.aliases.cum", defaultValue = "") String[] aliases, StatsService statsService, AnimeImageClient animeImageClient) {
      super("cum", aliases, statsService, animeImageClient);
   }

   @Override
   protected String getActionDescription() {
      return NSFWStrings.getRandomCum();
   }

   @Override
   protected String loadActionImageUrl1(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getImage(HM_NSFW.CUM);
   }

   @Override
   protected String loadActionImageUrl2(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getImage(HM_NSFW.CUM);
   }

   @Override
   protected String loadActionImageUrl3(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getImage(HM_NSFW.CUM);
   }
}


