package com.eme22.bolo.commands.general;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import com.eme22.bolo.Bot;
import com.eme22.bolo.language.LanguageService;
import com.jagrosh.jdautilities.command.UserContextMenu;
import com.jagrosh.jdautilities.command.UserContextMenuEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.modals.Modal;
import jakarta.inject.Singleton;

@Singleton
public class SendMessageAsContextMenu extends UserContextMenu {
   private final Bot bot;

   public SendMessageAsContextMenu(Bot bot) {
      this.name = "Send Message as this user";
      this.nameLocalization = Map.of(
         DiscordLocale.ENGLISH_US,
         "Send Message as this user",
         DiscordLocale.SPANISH,
         "Enviar mensaje como este usuario",
         DiscordLocale.SPANISH_LATAM,
         "Enviar mensaje como este usuario",
         DiscordLocale.ENGLISH_UK,
         "Send Message as this user"
      );
      this.guildOnly = true;
      this.bot = bot;
   }

   protected void execute(UserContextMenuEvent event) {
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      User usuario = event.getTargetMember().getUser();
      TextInput body = TextInput.create("body", TextInputStyle.PARAGRAPH).setMinLength(1).setMaxLength(1000).build();
      Modal modal = Modal.create("sendMessageAs-" + usuario.getIdLong(), languageService.getMessage("command.sendmessage.placeholder.title"))
         .addComponents(Label.of("Message", body))
         .build();
      event.replyModal(modal).queue();
   }

   private void sendFakeMessage(User usuario, String message, TextChannel textChannel) throws IOException {
      Member member = textChannel.getGuild().getMember(usuario);
      String avatarUrl;
      String name;
      if (member == null) {
         avatarUrl = usuario.getEffectiveAvatarUrl();
         name = usuario.getName();
      } else {
         avatarUrl = member.getEffectiveAvatarUrl();
         name = member.getEffectiveName();
      }

      URL url = java.net.URI.create(avatarUrl).toURL();
      Webhook webhook = textChannel.createWebhook(name).setAvatar(Icon.from(new BufferedInputStream(url.openStream()))).complete();
      JDAWebhookClient client = JDAWebhookClient.from(webhook);

      try {
         client.send(message).thenRun(() -> webhook.delete().queue());
      } catch (Throwable var13) {
         if (client != null) {
            try {
               client.close();
            } catch (Throwable var12) {
               var13.addSuppressed(var12);
            }
         }

         throw var13;
      }

      if (client != null) {
         client.close();
      }
   }
}


