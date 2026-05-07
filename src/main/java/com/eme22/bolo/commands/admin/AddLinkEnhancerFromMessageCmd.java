package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.language.LanguageService;
import com.jagrosh.jdautilities.command.MessageContextMenu;
import com.jagrosh.jdautilities.command.MessageContextMenuEvent;
import com.jagrosh.jdautilities.command.Command.Category;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.modals.Modal;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class AddLinkEnhancerFromMessageCmd extends MessageContextMenu {

    private final Bot bot;

    @jakarta.inject.Inject
    public AddLinkEnhancerFromMessageCmd(@Named("adminCategory") Category category, Bot bot) {
        //this.category = category;
        this.bot = bot;
        this.name = "Extract Link Enhancer";
        this.nameLocalization = Map.of(
                DiscordLocale.ENGLISH_UK, "Extract Link Enhancer",
                DiscordLocale.ENGLISH_US, "Extract Link Enhancer",
                DiscordLocale.SPANISH, "Extraer mejorador de enlaces",
                DiscordLocale.SPANISH_LATAM, "Extraer mejorador de enlaces"
        );
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.guildOnly = true;
    }

    @Override
    protected void execute(MessageContextMenuEvent event) {
        LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());
        String content = event.getTarget().getContentRaw();

        // Simple URL extraction
        Pattern urlPattern = Pattern.compile("https?://\\S+");
        Matcher matcher = urlPattern.matcher(content);

        if (!matcher.find()) {
            event.reply(lang.getErrorMessage("linkenhancer.nourlfound")).setEphemeral(true).queue();
            return;
        }

        String url = matcher.group();
        String linkRegex = "https?://(www\\.)?" + getDomain(url) + "/.*";
        String enhancerRegex = getDomain(url).replace(".", "\\.");
        String enhancerReplacement = getSuggestedReplacement(url);

        TextInput linkRegexInput = TextInput.create("link_regex", TextInputStyle.SHORT)
                .setValue(linkRegex)
                .setRequired(true)
                .build();

        TextInput enhancerRegexInput = TextInput.create("enhancer_regex", TextInputStyle.SHORT)
                .setValue(enhancerRegex)
                .setRequired(true)
                .build();

        TextInput enhancerReplacementInput = TextInput.create("enhancer_replacement", TextInputStyle.SHORT)
                .setValue(enhancerReplacement)
                .setRequired(true)
                .build();

        Modal modal = Modal.create("link_enhancer_add_modal", lang.getMessage("linkenhancer.modal.title"))
                .addComponents(
                        Label.of(lang.getMessage("linkenhancer.modal.linkregex"), linkRegexInput),
                        Label.of(lang.getMessage("linkenhancer.modal.enhancerregex"), enhancerRegexInput),
                        Label.of(lang.getMessage("linkenhancer.modal.replacement"), enhancerReplacementInput)
                )
                .build();

        event.replyModal(modal).queue();
    }

    private String getDomain(String url) {
        try {
            java.net.URI uri = java.net.URI.create(url);
            String domain = uri.getHost();
            if (domain != null && domain.startsWith("www.")) {
                domain = domain.substring(4);
            }
            return domain != null ? domain : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String getSuggestedReplacement(String url) {
        if (url.contains("tiktok.com")) return "vxtiktok.com";
        if (url.contains("twitter.com") || url.contains("x.com")) return "fxtwitter.com";
        if (url.contains("instagram.com")) return "ddinstagram.com";
        if (url.contains("reddit.com")) return "rxddit.com";
        return "";
    }
}
