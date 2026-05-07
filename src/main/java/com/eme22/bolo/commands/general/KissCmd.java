package com.eme22.bolo.commands.general;

import com.eme22.bolo.nsfw.NSFWStrings;
import com.eme22.bolo.stats.StatsService;
import com.eme22.imageapi.AnimeImageClient;
import com.eme22.imageapi.util.Endpoints.NEKO;
import com.eme22.imageapi.util.Endpoints.WAIFU_SFW;
import java.io.IOException;
import java.net.URISyntaxException;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class KissCmd extends ActionsCmd {
   @Inject
   public KissCmd(@ConfigProperty(name = "config.aliases.kiss", defaultValue = "") String[] aliases, StatsService statsService, AnimeImageClient animeImageClient) {
      super("besar", aliases, statsService, animeImageClient);
      this.name = "kiss";
   }

   @Override
   protected String getActionDescription() {
      return NSFWStrings.getRandomKiss();
   }

   @Override
   protected String loadActionImageUrl1(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getWaifuEndPoint(WAIFU_SFW.KISS);
   }

   @Override
   protected String loadActionImageUrl2(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getNekosEndPoint(NEKO.KISS);
   }

   @Override
   protected String loadActionImageUrl3(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getNekosEndPoint(NEKO.KISS);
   }
}


