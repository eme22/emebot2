package com.eme22.bolo.commands.admin;

import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import com.jagrosh.jdautilities.menu.Paginator.Builder;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class ListLinkEnhancerCmd extends AdminCommand {
   private final Builder builder;
   private final Bot bot;
   @ConfigProperty(name = "config.aliases.listlinkenhancer", defaultValue = "")
   String[] aliases = new String[0];

   public ListLinkEnhancerCmd(@Named("adminCategory") Category category, Bot bot) {
      super(category);
      this.name = "listlinkenhancer";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "listlinkenhancer",
         DiscordLocale.ENGLISH_US,
         "listlinkenhancer",
         DiscordLocale.SPANISH,
         "listarmejoradordeenlaces",
         DiscordLocale.SPANISH_LATAM,
         "listarmejoradordeenlaces"
      );
      this.help = "lists a regular expression from the list of regular expressions of the link enhancer";
      this.descriptionLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "lists a regular expression from the list of regular expressions of the link enhancer",
         DiscordLocale.ENGLISH_US,
         "lists a regular expression from the list of regular expressions of the link enhancer",
         DiscordLocale.SPANISH,
         "Lista la lista de expresiones regulares del mejorador de enlaces",
         DiscordLocale.SPANISH_LATAM,
         "Lista la lista de expresiones regulares del mejorador de enlaces"
      );
      this.bot = bot;
      this.guildOnly = true;
      this.builder = (Builder)((Builder)new Builder().setColumns(2).setFinalAction(m -> {
            try {
               m.clearReactions().queue();
            } catch (PermissionException var2) {
            }
         }).setItemsPerPage(10).waitOnSinglePage(false).useNumberedItems(true).showPageNumbers(true).wrapPageEnds(true).setEventWaiter(bot.getWaiter()))
         .setTimeout(1L, TimeUnit.MINUTES);
   }

   protected void execute(SlashCommandEvent event) {
      Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      String[] items = settings.getLinkEnhancers()
         .stream()
         .map(linkEnhancer -> linkEnhancer.getId() + ": " + linkEnhancer.getLinkEnhancerEnhancerRegex() + " - " + linkEnhancer.getLinkEnhancerReplacement())
         .toArray(String[]::new);
      this.builder.setText((i1, i2) -> languageService.getMessage("listlinkenhancer.title", new Object[]{i1, i2})).setItems(items);
      this.builder.build().paginate(event.getChannel(), 0);
      event.reply(languageService.getMessage("listlinkenhancer.opened")).setEphemeral(true).queue();
   }

   protected void execute(CommandEvent event) {
      Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      String[] items = settings.getLinkEnhancers()
         .stream()
         .map(linkEnhancer -> linkEnhancer.getId() + ": " + linkEnhancer.getLinkEnhancerEnhancerRegex() + " - " + linkEnhancer.getLinkEnhancerReplacement())
         .toArray(String[]::new);
      this.builder.setText((i1, i2) -> languageService.getMessage("listlinkenhancer.title", new Object[]{i1, i2})).setItems(items);
      this.builder.build().paginate(event.getChannel(), 0);
      event.replySuccess(languageService.getMessage("listlinkenhancer.opened"), message -> message.delete().queueAfter(5L, TimeUnit.SECONDS));
   }
}



