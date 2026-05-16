package com.eme22.bolo.commands.music;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.audio.QueuedTrack;
import com.eme22.bolo.commands.MusicCommand;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.model.RepeatMode;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.utils.FormatUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.menu.Paginator.Builder;
import dev.arbjerg.lavalink.client.player.Track;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class QueueCmd extends MusicCommand {
   private final Builder builder;
   private final Bot bot;
   @ConfigProperty(name = "config.aliases.queue", defaultValue = "")
   String[] aliases = new String[0];

   public QueueCmd(Bot bot) {
      super(bot);
      this.name = "queue";
      this.help = "shows the current queue";
      this.bot = bot;
      this.arguments = "[pagenum]";
      this.bePlaying = true;
      this.botPermissions = new Permission[]{Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS};
      this.builder = (Builder)((Builder)new Builder().setColumns(1).setFinalAction(m -> {
            try {
               m.clearReactions().queue();
            } catch (PermissionException var2) {
            }
         }).setItemsPerPage(10).waitOnSinglePage(false).useNumberedItems(true).showPageNumbers(true).wrapPageEnds(true).setEventWaiter(bot.getWaiter()))
         .setTimeout(1L, TimeUnit.MINUTES);
      this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "pagina", "pagina de la cola").setRequired(false));
   }

   @Override
   public void doCommand(CommandEvent event) {
      int pagenum = 1;

      try {
         pagenum = Integer.parseInt(event.getArgs());
      } catch (NumberFormatException var12) {
      }

      AudioHandler ah = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      LanguageService strings = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      List<QueuedTrack> list = ah.getQueueManager().getQueue().getList();
      if (list.isEmpty()) {
         MessageCreateData nowp = ah.getNowPlaying(event.getJDA());
         MessageCreateData nonowp = ah.getNoMusicPlaying(event.getJDA(), strings);
         MessageCreateData built = ((MessageCreateBuilder)((MessageCreateBuilder)new MessageCreateBuilder()
                  .setContent(event.getClient().getWarning() + " No hay musica en cola!"))
               .setEmbeds(new MessageEmbed[]{(MessageEmbed)(nowp == null ? nonowp : nowp).getEmbeds().get(0)}))
            .build();
         event.reply(built);

      } else {
         event.reply(ah.getQueueMessage(event.getJDA(), pagenum, event.getGuild()));

      }
   }

   @Override
   public void doCommand(SlashCommandEvent event) {
      int pagenum = 1;

      try {
         pagenum = Integer.parseInt(Objects.requireNonNull(event.getOption("pagina")).getAsString());
      } catch (NumberFormatException | NullPointerException var12) {
      }

      AudioHandler ah = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      LanguageService strings = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      List<QueuedTrack> list = ah.getQueueManager().getQueue().getList();
      if (list.isEmpty()) {
         MessageCreateData nowp = ah.getNowPlaying(event.getJDA());
         MessageCreateData nonowp = ah.getNoMusicPlaying(event.getJDA(), strings);
         MessageCreateData built = ((MessageCreateBuilder)((MessageCreateBuilder)new MessageCreateBuilder()
                  .setContent(event.getClient().getWarning() + " No hay musica en cola!"))
               .setEmbeds(new MessageEmbed[]{(MessageEmbed)(nowp == null ? nonowp : nowp).getEmbeds().get(0)}))
            .build();
         event.reply(built).queue();

      } else {
         event.reply(ah.getQueueMessage(event.getJDA(), pagenum, event.getGuild())).queue();
      }
   }

   public String getQueueTitle(AudioHandler ah, String success, int songslength, long total, RepeatMode repeatmode) {
      StringBuilder sb = new StringBuilder();
      Track nowplaying = ah.getAudioPlayer().get().getTrack();
      if (nowplaying != null) {
         sb.append(ah.getAudioPlayer().get().getPaused() ? "â¸" : "â–¶")
            .append(" **")
            .append(ah.getAudioPlayer().get().getTrack().getInfo().getTitle())
            .append("**\n");
      }

      return FormatUtil.filter(
         sb.append(success)
            .append(" Cola Actual | ")
            .append(songslength)
            .append(" entradas | Tiempo total: `")
            .append(FormatUtil.formatTime(total))
            .append("` ")
            .append(repeatmode.getEmoji() != null ? "| " + repeatmode.getEmoji() : "")
            .toString()
      );
   }
}







