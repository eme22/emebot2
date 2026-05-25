package com.eme22.bolo.commands.general;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class SendMessageAsCmd extends BaseCommand {
   @ConfigProperty(name = "config.aliases.sendmessageas", defaultValue = "")
   String[] aliases = new String[0];

   public SendMessageAsCmd(Bot bot) {
      this.name = "sendmessageas";
      this.help = "envia un mensaje como el usuario seleccionado";
      this.arguments = "[usuario] mensaje";
      this.options = Arrays.asList(
         new OptionData(OptionType.USER, "usuario", "busca el usuario a hacerce pasar.").setRequired(true),
         new OptionData(OptionType.STRING, "mensaje", "mensaje a decir").setRequired(true)
      );
      this.guildOnly = true;
   }

   public void execute(SlashCommandEvent event) {
      event.deferReply(true).queue(hook -> {
         String message = event.getOption("mensaje").getAsString();
         User usuario = event.getOption("usuario").getAsUser();

         this.sendFakeMessage(usuario, message, event.getTextChannel())
             .thenRun(() -> hook.editOriginal(event.getClient().getSuccess() + " Mensaje Enviado").queue())
             .exceptionally(throwable -> {
                hook.editOriginal(event.getClient().getError() + " Error al enviar el mensaje").queue();
                return null;
             });
      });
   }

   private CompletableFuture<Void> sendFakeMessage(User usuario, String message, TextChannel textChannel) {
      CompletableFuture<Void> future = new CompletableFuture<>();

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

      CompletableFuture.runAsync(() -> {
         try {
            URL url = java.net.URI.create(avatarUrl).toURL();
            Icon avatar = Icon.from(new BufferedInputStream(url.openStream()));
            textChannel.createWebhook(name).setAvatar(avatar).queue(webhook -> {
               JDAWebhookClient client = JDAWebhookClient.from(webhook);
               client.send(message)
                  .thenRun(() -> {
                     webhook.delete().queue(
                        v -> {
                           client.close();
                           future.complete(null);
                        },
                        t -> {
                           client.close();
                           future.complete(null);
                        }
                     );
                  })
                  .exceptionally(throwable -> {
                     webhook.delete().queue(
                        v -> client.close(),
                        t -> client.close()
                     );
                     future.completeExceptionally(throwable);
                     return null;
                  });
            }, throwable -> {
               future.completeExceptionally(throwable);
            });
         } catch (IOException e) {
            future.completeExceptionally(e);
         }
      });

      return future;
   }

   public void execute(CommandEvent event) {
      if (event.getArgs().isEmpty()) {
         event.replyError(" Por favor incluya al menos un usuario y mensaje");
      } else {
         String[] data = event.getArgs().split("] ");
         if (data.length != 2) {
            event.replyError(" Parametros incorrectos");
         } else {
            User usuario = (User)FinderUtil.findUsers(data[0].substring(1).trim(), event.getJDA()).get(0);
            String message = data[1];

            this.sendFakeMessage(usuario, message, event.getTextChannel())
                .thenRun(() -> event.getMessage().delete().queue())
                .exceptionally(throwable -> {
                    event.replyError(" Error al enviar el mensaje");
                    return null;
                });
         }
      }
   }
}










