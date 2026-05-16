package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import com.eme22.bolo.nsfw.NSFWStrings;
import com.eme22.bolo.stats.StatsService;
import com.eme22.imageapi.AnimeImageClient;
import com.eme22.imageapi.util.Endpoints.WAIFU_SFW;
import java.io.IOException;
import java.net.URISyntaxException;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class BiteCmd extends ActionsCmd {
   @Inject
   public BiteCmd(@ConfigProperty(name = "config.aliases.bite", defaultValue = "") String[] aliases, StatsService statsService, AnimeImageClient animeImageClient) {
      super("muerde", aliases, statsService, animeImageClient);
      this.name = "bite";
   }

   @Override
   protected String getActionDescription() {
      return NSFWStrings.getRandomBite();
   }

   @Override
   protected String loadActionImageUrl1(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getImage(WAIFU_SFW.BITE);
   }

   @Override
   protected String loadActionImageUrl2(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getImage(WAIFU_SFW.BITE);
   }

   @Override
   protected String loadActionImageUrl3(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getImage(WAIFU_SFW.BITE);
   }
}







