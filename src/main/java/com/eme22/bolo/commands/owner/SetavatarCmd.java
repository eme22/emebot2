package com.eme22.bolo.commands.owner;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.OwnerCommand;
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class SetavatarCmd extends OwnerCommand {
   @ConfigProperty(name = "config.aliases.setavatar", defaultValue = "")
   String[] aliases = new String[0];
   @ConfigProperty(name = "config.clientToken", defaultValue = "")
   String clientToken;

   public SetavatarCmd(Bot bot) {
      this.name = "setavatar";
      this.help = "sets the avatar of the bot";
      this.arguments = "<url>";
      this.guildOnly = false;
      this.options = Collections.singletonList(new OptionData(OptionType.STRING, "url", "Setea el avatar del bot").setRequired(true));
   }

   public void execute(SlashCommandEvent event) {
      String url = event.optString("url", null);

      try {
         InputStream s = OtherUtil.imageFromUrl(url, this.clientToken);
         if (s == null) {
            event.reply(event.getClient().getError() + " Url Invalida!!!").queue();
            return;
         }

         event.getJDA()
            .getSelfUser()
            .getManager()
            .setAvatar(Icon.from(s))
            .queue(
               v -> event.reply(event.getClient().getSuccess() + " Successfully changed avatar.").queue(),
               t -> event.reply(event.getClient().getError() + " Failed to set avatar.").queue()
            );
      } catch (IOException var4) {
         event.reply(event.getClient().getError() + " Could not load from provided URL.").queue();
      }
   }

   public void execute(CommandEvent event) {
      String url;
      if (event.getArgs().isEmpty()) {
         if (!event.getMessage().getAttachments().isEmpty() && ((Attachment)event.getMessage().getAttachments().get(0)).isImage()) {
            url = ((Attachment)event.getMessage().getAttachments().get(0)).getUrl();
         } else {
            url = null;
         }
      } else {
         url = event.getArgs();
      }

      InputStream s = OtherUtil.imageFromUrl(url, this.clientToken);
      if (s == null) {
         event.replyError(" Invalid or missing URL");
      } else {
         try {
            event.getSelfUser()
               .getManager()
               .setAvatar(Icon.from(s))
               .queue(v -> event.replySuccess(" Successfully changed avatar."), t -> event.replyError(" Failed to set avatar."));
         } catch (IOException var5) {
            event.replyError(" Could not load from provided URL.");
         }
      }
   }
}










