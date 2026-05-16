package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import com.eme22.bolo.nsfw.NSFWStrings;
import com.eme22.bolo.stats.StatsService;
import com.eme22.imageapi.AnimeImageClient;
import com.eme22.imageapi.util.Endpoints.WAIFU_SFW;
import java.io.IOException;
import java.net.URISyntaxException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class PokeCmd extends ActionsCmd {
   public PokeCmd(@ConfigProperty(name = "config.aliases.poke", defaultValue = "") String[] aliases, StatsService statsService, AnimeImageClient animeImageClient) {
      super("toca", aliases, statsService, animeImageClient);
      this.name = "poke";
   }

   @Override
   protected String getActionDescription() {
      return NSFWStrings.getRandomPoke();
   }

   @Override
   protected String loadActionImageUrl1(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getImage(WAIFU_SFW.POKE);
   }

   @Override
   protected String loadActionImageUrl2(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getImage(WAIFU_SFW.POKE);
   }

   @Override
   protected String loadActionImageUrl3(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getImage(WAIFU_SFW.POKE);
   }
}







