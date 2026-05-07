package com.eme22.bolo.commands.general;

import com.eme22.bolo.nsfw.NSFWStrings;
import com.eme22.bolo.stats.StatsService;
import com.eme22.imageapi.AnimeImageClient;
import com.eme22.imageapi.util.Endpoints.HM_SFW;
import com.eme22.imageapi.util.Endpoints.NEKO;
import com.eme22.imageapi.util.Endpoints.WAIFU_SFW;
import java.io.IOException;
import java.net.URISyntaxException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class SlapCmd extends ActionsCmd {
   public SlapCmd(@ConfigProperty(name = "config.aliases.slap", defaultValue = "") String[] aliases, StatsService statsService, AnimeImageClient animeImageClient) {
      super("cachetear", aliases, statsService, animeImageClient);
      this.name = "slap";
      this.success = new ActionsCmd.Consumer<InteractionHook>() {
         public void accept(InteractionHook success) {
            super.accept(success);
            statsService.updateSlaps(success.getInteraction().getGuild().getIdLong());
         }
      };
      this.success1 = new ActionsCmd.Consumer<Message>() {
         public void accept(Message success) {
            super.accept(success);
            statsService.updateSlaps(success.getGuild().getIdLong());
         }
      };
   }

   @Override
   protected String getActionDescription() {
      return NSFWStrings.getRandomSlap();
   }

   @Override
   protected String loadActionImageUrl1(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getImage(WAIFU_SFW.SLAP);
   }

   @Override
   protected String loadActionImageUrl2(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getImage(HM_SFW.SLAP);
   }

   @Override
   protected String loadActionImageUrl3(AnimeImageClient animeImageClient) throws IOException, URISyntaxException, InterruptedException {
      return animeImageClient.getImage(NEKO.SLAP);
   }
}


