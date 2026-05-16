package com.eme22.bolo.commands.admin;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Named;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.model.LinkEnhancer;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class AddLinkEnhancerCmd extends AdminCommand {
   private final Bot bot;
   @ConfigProperty(name = "config.aliases.addlinkenhancer", defaultValue = "")
   String[] aliases = new String[0];

   public AddLinkEnhancerCmd(@Named("adminCategory") Category category, Bot bot) {
      super(category);
      this.name = "addlinkenhancer";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "addlinkenhancer",
         DiscordLocale.ENGLISH_US,
         "addlinkenhancer",
         DiscordLocale.SPANISH,
         "agregarmejoradordeenlaces",
         DiscordLocale.SPANISH_LATAM,
         "agregarmejoradordeenlaces"
      );
      this.help = "Agrega una expresion regular a la lista de expresiones regulares del mejorador de enlaces";
      this.descriptionLocalization = Map.of(
         DiscordLocale.ENGLISH_UK,
         "Adds a regular expression to the list of regular expressions of the link enhancer",
         DiscordLocale.ENGLISH_US,
         "Adds a regular expression to the list of regular expressions of the link enhancer",
         DiscordLocale.SPANISH,
         "Agrega una expresion regular a la lista de expresiones regulares del mejorador de enlaces",
         DiscordLocale.SPANISH_LATAM,
         "Agrega una expresion regular a la lista de expresiones regulares del mejorador de enlaces"
      );
      this.options = List.of(
         new OptionData(OptionType.STRING, "linkregex", "Expresion regular para el link").setRequired(true).setAutoComplete(true),
         new OptionData(OptionType.STRING, "enhancerregex", "Expresion regular para el mejorador (Opcional si usas preset)").setRequired(false),
         new OptionData(OptionType.STRING, "enhancerreplacement", "Reemplazo para el mejorador (Opcional si usas preset)").setRequired(false)
      );
      this.bot = bot;
      this.guildOnly = true;
   }

   public void execute(SlashCommandEvent event) {
      Server s = this.bot.getSettingsManager().getSettings(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      String linkRegex = event.getOption("linkregex").getAsString();
      
      String enhancerRegex = event.getOption("enhancerregex") != null ? event.getOption("enhancerregex").getAsString() : null;
      String enhancerReplacement = event.getOption("enhancerreplacement") != null ? event.getOption("enhancerreplacement").getAsString() : null;

      // Handle Presets
      if (enhancerRegex == null || enhancerReplacement == null) {
         if (linkRegex.contains("tiktok.com")) {
            enhancerRegex = "tiktok\\.com";
            enhancerReplacement = "vxtiktok.com";
         } else if (linkRegex.contains("twitter.com") || linkRegex.contains("x.com")) {
            enhancerRegex = "(twitter|x)\\.com";
            enhancerReplacement = "fxtwitter.com";
         } else if (linkRegex.contains("instagram.com")) {
            enhancerRegex = "instagram\\.com";
            enhancerReplacement = "ddinstagram.com";
         } else if (linkRegex.contains("reddit.com")) {
            enhancerRegex = "reddit\\.com";
            enhancerReplacement = "rxddit.com";
         } else {
            event.reply(event.getClient().getError() + " " + lang.getMessage("linkenhancer.missingargs")).setEphemeral(true).queue();
            return;
         }
      }

      try {
         Pattern.compile(linkRegex);
      } catch (PatternSyntaxException var9) {
         event.reply(event.getClient().getError() + " " + lang.getMessage("linkenhancer.invalidregex", new Object[]{linkRegex})).queue();
         return;
      }

      try {
         Pattern.compile(enhancerRegex);
      } catch (PatternSyntaxException var8) {
         event.reply(event.getClient().getError() + " " + lang.getMessage("linkenhancer.invalidregex", new Object[]{enhancerRegex})).queue();
         return;
      }

      LinkEnhancer linkEnhancer = new LinkEnhancer();
      linkEnhancer.setLinkEnhancerLinkRegex(linkRegex);
      linkEnhancer.setLinkEnhancerEnhancerRegex(enhancerRegex);
      linkEnhancer.setLinkEnhancerReplacement(enhancerReplacement);
      linkEnhancer.setServer(event.getGuild().getIdLong());
      s.addLinkEnhancer(linkEnhancer);
      s.persist();
      event.reply(event.getClient().getSuccess() + " " + lang.getMessage("linkenhancer.added.success")).queue();
   }

   @Override
   public void onAutoComplete(net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent event) {
      if (event.getFocusedOption().getName().equals("linkregex")) {
         List<Command.Choice> choices = List.of(
            new Command.Choice("TikTok (vxtiktok)", "https?://(www\\.)?tiktok\\.com/.*"),
            new Command.Choice("Twitter/X (fxtwitter)", "https?://(www\\.|mobile\\.)?(twitter|x)\\.com/.*"),
            new Command.Choice("Instagram (ddinstagram)", "https?://(www\\.)?instagram\\.com/(reels?|p)/.*"),
            new Command.Choice("Reddit (rxddit)", "https?://(www\\.)?reddit\\.com/.*")
         );
         event.replyChoices(choices).queue();
      }
   }

   public void execute(CommandEvent event) {

      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      Pattern pattern = Pattern.compile("<(.*?)> <(.*?)> <(.*?)>");
      Matcher matcher = pattern.matcher(event.getArgs());
      if (matcher.find() && matcher.groupCount() >= 3) {
         String linkRegex = matcher.group(1);
         String enhancerRegex = matcher.group(2);
         String enhancerReplacement = matcher.group(3);
         if (!linkRegex.isBlank() && !enhancerRegex.isBlank() && !enhancerReplacement.isBlank()) {
            try {
               Pattern.compile(linkRegex);
            } catch (PatternSyntaxException var11) {
               event.replyError(" " + lang.getMessage("linkenhancer.invalidargs.linkRegex", new Object[]{linkRegex}));
               return;
            }

            try {
               Pattern.compile(enhancerRegex);
            } catch (PatternSyntaxException var10) {
               event.replyError(" " + lang.getMessage("linkenhancer.invalidargs.linkRegex", new Object[]{enhancerRegex}));
               return;
            }

            LinkEnhancer linkEnhancer = new LinkEnhancer();
            linkEnhancer.setLinkEnhancerLinkRegex(linkRegex);
            linkEnhancer.setLinkEnhancerEnhancerRegex(enhancerRegex);
            linkEnhancer.setLinkEnhancerReplacement(enhancerReplacement);
            linkEnhancer.setServer(event.getGuild().getIdLong());
            Server s = this.bot.getSettingsManager().getSettings(event.getGuild());
            s.addLinkEnhancer(linkEnhancer);
            s.persist();
            event.replySuccess(lang.getMessage("linkenhancer.added.success"));
         } else {
            event.replyError(lang.getMessage("linkenhancer.invalidargs"));
         }
      } else {
         event.replyError(lang.getMessage("linkenhancer.invalidargs.not.all.args"));
      }
   }
}











