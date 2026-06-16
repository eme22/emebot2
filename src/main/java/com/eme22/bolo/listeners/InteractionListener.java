package com.eme22.bolo.listeners;

import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.entities.Pair;

import lombok.extern.slf4j.Slf4j;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import com.eme22.bolo.Bot;
import com.eme22.bolo.language.LanguageService;
import com.eme22.bolo.stats.StatsService;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import com.eme22.bolo.utils.OtherUtil;
import com.eme22.bolo.model.LinkEnhancer;
import com.eme22.bolo.model.RepeatMode;
import com.eme22.bolo.model.RoleManager;
import com.eme22.bolo.model.Server;
import com.jagrosh.jlyrics.Lyrics;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.NotNull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.transaction.Transactional;



@ApplicationScoped
@Slf4j
public class InteractionListener implements EventListener {
   
   private final Bot bot;
   @ConfigProperty(name = "config.update")
   boolean updatealerts;
   @ConfigProperty(name = "config.owner")
   long owner;
   @ConfigProperty(name = "config.clientToken", defaultValue = "")
   String clientToken;
   
   @ConfigProperty(name = "quarkus.application.version")
   String version;

   @ConfigProperty(name = "config.success")
   String successEmoji;
   @ConfigProperty(name = "config.warning")
   String warningEmoji;
   @ConfigProperty(name = "config.error")
   String errorEmoji;
   
   private final StatsService statsService;

   @Inject
   public InteractionListener(Bot bot, StatsService statsService) {
      this.bot = bot;
      this.statsService = statsService;
   }

   @Override
   public void onEvent(@NotNull GenericEvent event) {
       switch (event) {
           case ButtonInteractionEvent buttonInteractionEvent -> this.onButtonInteraction(buttonInteractionEvent);
           case EntitySelectInteractionEvent entitySelectInteractionEvent ->
                   this.onEntitySelectInteraction(entitySelectInteractionEvent);
           case StringSelectInteractionEvent stringSelectInteractionEvent ->
                   this.onStringSelectInteraction(stringSelectInteractionEvent);
           case ModalInteractionEvent modalInteractionEvent -> this.onModalInteraction(modalInteractionEvent);
           default -> {
           }
       }
   }

   @ActivateRequestContext
   @Transactional
   public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

      if (event.getUser().isBot()) return;
      if (event.getGuild() == null) return;
      String id = event.getComponentId();
      if (!id.startsWith("music:")) return;

      com.eme22.bolo.audio.AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());

      switch (id) {
         case "music:pause":
         AtomicBoolean paused = new AtomicBoolean(false);
         handler.getAudioPlayer().ifPresent(player -> {
               paused.set(!player.getPaused());
               player.setPaused(paused.get()).subscribe(p -> {
                  event.editMessage(MessageEditBuilder.fromMessage(event.getMessage())
                     .setEmbeds(handler.getNowPlaying(event.getJDA()).getEmbeds())
                     .build()).queue();
            });
         });
         break;
         case "music:skip":
             handler.getVotes().clear();
             handler.getAudioPlayer().ifPresentOrElse(player -> {
                 if (player.getTrack() == null) {
                     event.reply(lang.getMessage("music.no.music.playing", "⏹", "")).setEphemeral(true).queue();
                     return;
                 }
                 try {
                     com.eme22.bolo.audio.RequestMetadata rm = handler.getRequestMetadata();
                     
                     String addedBy;
                     if (rm.owner() == 0L) {
                         addedBy = lang.getMessage("command.music.autoplay");
                     } else {
                         User user = event.getJDA().getUserById(rm.user().id());
                         String mention = user != null ? user.getAsMention() : (rm.user().username() != null ? "**" + rm.user().username() + "**" : String.valueOf(rm.user().id()));
                         addedBy = lang.getMessage("command.music.added.by", mention);
                     }

                     String message = lang.getMessage(
                         "command.music.skipped",
                         successEmoji,
                         player.getTrack().getInfo().getTitle(),
                         addedBy,
                         event.getUser().getAsMention()
                     );
                     player.stopTrack().subscribe(p -> {
                         event.reply(message).queue();
                     });
                 } catch (IOException e) {
                     event.reply(lang.getSuccessMessage("command.skip.success")).setEphemeral(true).queue();
                     player.stopTrack().subscribe();
                 }
             }, () -> {
                 event.reply(lang.getMessage("music.no.music.playing", "⏹", "")).setEphemeral(true).queue();
             });
             break;
         case "music:stop":
            handler.stopAndClear();
            event.getMessage().editMessage(handler.disableButtons(event.getMessage())).queue();
            break;
         case "music:shuffle":
            handler.getQueueManager().shuffle();
            event.editMessage(MessageEditBuilder.fromMessage(event.getMessage())
                .setEmbeds(handler.getNowPlaying(event.getJDA()).getEmbeds())
                .setComponents(handler.getNowPlaying(event.getJDA()).getComponents())
                   .build()).queue();
            break;
         case "music:repeat":
               Server settings = this.bot.getSettingsManager().getSettings(event.getGuild());
               RepeatMode mode = settings.getRepeatMode();
               RepeatMode nextMode;
               if (mode == RepeatMode.OFF) nextMode = RepeatMode.SINGLE;
               else if (mode == RepeatMode.SINGLE) nextMode = RepeatMode.ALL;
               else nextMode = RepeatMode.OFF;
               
               settings.setRepeatMode(nextMode);
               this.bot.getSettingsManager().saveSettings(settings);
               event.editMessage(MessageEditBuilder.fromMessage(event.getMessage())
                .setEmbeds(handler.getNowPlaying(event.getJDA()).getEmbeds())
                .setComponents(handler.getNowPlaying(event.getJDA()).getComponents())
                   .build()).queue();
            break;
         case "music:queue":
            MessageCreateData queueMsg = handler.getQueueMessage(event.getJDA(), 1, event.getGuild());
            if (queueMsg == null) {
                event.reply(lang.getWarningMessage("command.queue.empty")).setEphemeral(true).queue();
            } else {
                event.reply(queueMsg).setEphemeral(true).queue();
            }
            break;
         case "music:mute":
            handler.toggleMute();
            event.editMessage(MessageEditBuilder.fromMessage(event.getMessage())
                .setComponents(handler.getNowPlaying(event.getJDA()).getComponents())
                   .build()).queue();
            break;
         case "music:lyrics":
            handler.getAudioPlayer().ifPresent(player -> {
                String title = player.getTrack().getInfo().getTitle();
                event.deferReply(true).queue(hook -> {
                    Lyrics lyrics = OtherUtil.getLyrics(title);
                    if (lyrics == null) {
                        hook.sendMessage(lang.getErrorMessage("command.lyrics.notfound")).setEphemeral(true).queue();
                    } else {
                        EmbedBuilder eb = new EmbedBuilder()
                            .setColor(event.getGuild().getSelfMember().getColor())
                            .setTitle(lyrics.getTitle(), lyrics.getURL())
                            .setDescription(lyrics.getContent().length() > 4000 ? lyrics.getContent().substring(0, 4000) : lyrics.getContent());
                        hook.sendMessageEmbeds(eb.build()).setEphemeral(true).queue();
                    }
                });
            });
            break;
         case "music:effects":
            StringSelectMenu menu = StringSelectMenu.create("music:effect_select")
                .setPlaceholder(lang.getMessage("command.effects.placeholder"))
                .addOption(lang.getMessage("effect.none.label"), "effect_none", lang.getMessage("effect.none.desc"), Emoji.fromFormatted("⏹️"))
                .addOption(lang.getMessage("effect.bassboost.label"), "effect_bassboost", lang.getMessage("effect.bassboost.desc"), Emoji.fromFormatted("🎸"))
                .addOption(lang.getMessage("effect.nightcore.label"), "effect_nightcore", lang.getMessage("effect.nightcore.desc"), Emoji.fromFormatted("⚡"))
                .addOption(lang.getMessage("effect.vaporwave.label"), "effect_vaporwave", lang.getMessage("effect.vaporwave.desc"), Emoji.fromFormatted("🌊"))
                .addOption(lang.getMessage("effect.karaoke.label"), "effect_karaoke", lang.getMessage("effect.karaoke.desc"), Emoji.fromFormatted("🎤"))
                .addOption(lang.getMessage("effect.distortion.label"), "effect_distortion", lang.getMessage("effect.distortion.desc"), Emoji.fromFormatted("🔊"))
                .addOption(lang.getMessage("effect.chipmunk.label"), "effect_chipmunk", lang.getMessage("effect.chipmunk.desc"), Emoji.fromFormatted("🐿️"))
                .addOption(lang.getMessage("effect.alien.label"), "effect_alien", lang.getMessage("effect.alien.desc"), Emoji.fromFormatted("👽"))
                .addOption(lang.getMessage("effect.8d.label"), "effect_8d", lang.getMessage("effect.8d.desc"), Emoji.fromFormatted("🎧"))
                .addOption(lang.getMessage("effect.underwater.label"), "effect_underwater", lang.getMessage("effect.underwater.desc"), Emoji.fromFormatted("🫧"))
                .addOption(lang.getMessage("effect.demon.label"), "effect_demon", lang.getMessage("effect.demon.desc"), Emoji.fromFormatted("😈"))
                .addOption(lang.getMessage("effect.radio.label"), "effect_radio", lang.getMessage("effect.radio.desc"), Emoji.fromFormatted("📻"))
                .build();
            event.reply(lang.getMessage("command.effects.menu.title")).setComponents(ActionRow.of(menu)).setEphemeral(true).queue();
            break;
         default:
            if (id.startsWith("music:queue:page:")) {
                    int page = Integer.parseInt(id.substring("music:queue:page:".length()));
                    MessageCreateData msg = handler.getQueueMessage(event.getJDA(), page, event.getGuild());
                    if (msg != null) {
                    event.editMessage(MessageEditBuilder.fromMessage(event.getMessage())
                            .setEmbeds(msg.getEmbeds())
                            .setComponents(msg.getComponents())
                            .build()).queue();
                    }
            } else if (id.equals("music:queue:clear")) {
                if (OtherUtil.checkDJPermission(this.bot, event.getMember())) {
                        handler.getQueueManager().clear();
                    event.editMessage(lang.getSuccessMessage("command.queue.cleared"))
                            .setEmbeds(java.util.Collections.emptyList())
                            .setComponents(java.util.Collections.emptyList())
                            .queue();
                } else {
                    event.reply(lang.getErrorMessage("command.dj.only")).setEphemeral(true).queue();
                }
            }
            break;
      }
   }

   @ActivateRequestContext
   @Transactional
   public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
      if (event.getUser().isBot()) return;
      if (event.getGuild() == null) return;

      if (event.getComponentId().equals("linkenhancer:add-channels")) {
         this.handleAddLinkEnhancerChannels(event);
      }
   }

   private void handleAddLinkEnhancerChannels(EntitySelectInteractionEvent event) {
      com.eme22.bolo.model.Server s = this.bot.getSettingsManager().getSettings(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());

      List<Long> selectedIds = event.getValues().stream()
              .map(ISnowflake::getIdLong)
              .toList();

      for (Long id : selectedIds) {
         if (!s.getLinkEnhancerChannels().contains(id)) {
            s.addToLinkEnhancerChannels(id);
         }
      }

      s.persist();

      String mentions = selectedIds.stream()
              .map(id -> "<#" + id + ">")
              .collect(java.util.stream.Collectors.joining(", "));

      event.reply((event.getJDA().getPresence().getActivity() != null ? successEmoji + " " : "") + lang.getMessage("linkenhancer.manage.success", new Object[]{mentions}))
              .setEphemeral(true)
              .queue();
   }

   @ActivateRequestContext
   @Transactional
   public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
      if (!event.getUser().isBot()) {
         if (event.getGuild() != null) {
            if (event.getComponentId().equals("setlang")) {
               this.changeServerLanguage(event);
            }

            if (event.getComponentId().equals("roleselect")) {
               RoleManager manager = this.bot.getSettingsManager().getSettings(event.getGuild().getIdLong()).getRoleManager(event.getMessageIdLong());
               if (manager != null) {
                  java.util.Set<String> allowedRoles = manager.getEmoji().keySet();
                  List<String> selectedRoles = event.getValues();

                  event.deferReply(true).queue(hook -> {
                     java.util.List<String> added = new java.util.ArrayList<>();
                     java.util.List<String> removed = new java.util.ArrayList<>();
                     java.util.List<String> errors = new java.util.ArrayList<>();

                     for (String roleIdStr : allowedRoles) {
                        try {
                           long roleId = Long.parseLong(roleIdStr);
                           Role role = event.getGuild().getRoleById(roleId);
                           if (role == null) continue;

                           boolean hasRole = event.getMember().getRoles().contains(role);
                           boolean shouldHaveRole = selectedRoles.contains(roleIdStr);

                           if (shouldHaveRole && !hasRole) {
                              if (event.getGuild().getSelfMember().canInteract(role)) {
                                 event.getGuild().addRoleToMember(event.getMember(), role).queue();
                                 added.add(role.getName());
                              } else {
                                 errors.add(role.getName());
                              }
                           } else if (!shouldHaveRole && hasRole) {
                              if (event.getGuild().getSelfMember().canInteract(role)) {
                                 event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
                                 removed.add(role.getName());
                              } else {
                                 errors.add(role.getName());
                              }
                           }
                        } catch (Exception e) {
                           log.error("Error al procesar asignación de rol: " + roleIdStr, e);
                        }
                     }

                     StringBuilder response = new StringBuilder("¡Tus roles han sido actualizados!\n");
                     if (!added.isEmpty()) {
                        response.append("🟢 **Añadidos:** ").append(String.join(", ", added)).append("\n");
                     }
                     if (!removed.isEmpty()) {
                        response.append("🔴 **Removidos:** ").append(String.join(", ", removed)).append("\n");
                     }
                     if (!errors.isEmpty()) {
                        response.append("⚠️ **No se pudieron modificar (por permisos del bot):** ").append(String.join(", ", errors)).append("\n");
                     }
                     if (added.isEmpty() && removed.isEmpty() && errors.isEmpty()) {
                        response.append("No se realizaron cambios en tus roles.");
                     }

                     hook.sendMessage(response.toString()).queue();
                  });
               } else {
                  event.reply("Este panel de roles ya no está activo.").setEphemeral(true).queue();
               }
            }

            if (event.getComponentId().equals("music:effect_select")) {
                AudioHandler handler = this.bot.getPlayerManager().getAudioHandler(event.getGuild());
                LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());

                String selected = event.getValues().getFirst();
                handler.setEffect(selected);
                
                String effectName = lang.getMessage(selected.replace("_", ".") + ".label");
                event.editMessage(lang.getSuccessMessage("command.effects.applied", effectName))
                        .setComponents(java.util.Collections.emptyList())
                        .queue();

                Pair<Long, Long> lastNP = this.bot.getPlayerManager().getNowplayingHandler().getLastNP(event.getGuild());
                if (lastNP != null) {
                    TextChannel tc = event.getJDA().getTextChannelById(lastNP.getKey());
                    if (tc != null) {
                        tc.editMessageById(lastNP.getValue(), MessageEditBuilder.fromCreateData(handler.getNowPlaying(event.getJDA())).build()).queue(m -> {}, t -> {});
                    }
                }
            }
         }
      }
   }

   @ActivateRequestContext
   @Transactional
   public void onModalInteraction(@NotNull ModalInteractionEvent event) {
      if (!event.getUser().isBot()) {
         if (event.getGuild() != null) {
            if (event.getModalId().contains("sendMessageAs")) {
               this.sendMessageAs(event);
            }

            if (event.getModalId().equals("link_enhancer_add_modal")) {
               this.handleLinkEnhancerAddModal(event);
            }
         }
      }
   }

   private void handleLinkEnhancerAddModal(ModalInteractionEvent event) {
      Server s = this.bot.getSettingsManager().getSettings(event.getGuild());
      LanguageService lang = this.bot.getSettingsManager().getLanguageService(event.getGuild());

      String linkRegex = event.getValue("link_regex").getAsString();
      String enhancerRegex = event.getValue("enhancer_regex").getAsString();
      String enhancerReplacement = event.getValue("enhancer_replacement").getAsString();

      try {
         Pattern.compile(linkRegex);
      } catch (PatternSyntaxException e) {
         event.reply(errorEmoji + " " + event.getJDA().getSelfUser().getAsMention() + " " + lang.getMessage("linkenhancer.invalidregex", new Object[]{linkRegex})).setEphemeral(true).queue();
         return;
      }

      try {
         Pattern.compile(enhancerRegex);
      } catch (PatternSyntaxException e) {
         event.reply(errorEmoji + " " + event.getJDA().getSelfUser().getAsMention() + " " + lang.getMessage("linkenhancer.invalidregex", new Object[]{enhancerRegex})).setEphemeral(true).queue();
         return;
      }

      LinkEnhancer linkEnhancer = new LinkEnhancer();
      linkEnhancer.setLinkEnhancerLinkRegex(linkRegex);
      linkEnhancer.setLinkEnhancerEnhancerRegex(enhancerRegex);
      linkEnhancer.setLinkEnhancerReplacement(enhancerReplacement);
      linkEnhancer.setServer(event.getGuild().getIdLong());
      s.addLinkEnhancer(linkEnhancer);
      s.persist();
      event.reply(lang.getSuccessMessage("linkenhancer.added.success")).setEphemeral(true).queue();
   }

   private void sendMessageAs(ModalInteractionEvent event) {
      User user = event.getJDA().getUserById(Long.parseLong(event.getModalId().split("-")[1]));
      String body = ((ModalMapping)event.getValues().get(0)).getAsString();
      LanguageService languageService = this.bot.getSettingsManager().getLanguageService(event.getGuild());
      TextChannel textChannel = event.getJDA().getTextChannelById(event.getChannel().getId());

      event.deferReply(true).queue(hook -> {
         this.sendFakeMessage(user, body, textChannel)
             .thenRun(() -> hook.sendMessage(languageService.getSuccessMessage("command.sendmessage.success")).queue())
             .exceptionally(throwable -> {
                log.error("Error sending message", throwable);
                hook.sendMessage(languageService.getErrorMessage("command.sendmessage.error")).queue();
                return null;
             });
      });
   }

   private void changeServerLanguage(@NotNull StringSelectInteractionEvent event) {
      String lang = event.getValues().getFirst();
      this.bot.getSettingsManager().setLanguage(lang, event.getGuild());
      event.reply(successEmoji + " " + this.getString(event.getGuild().getIdLong(), "command.setlang.success", ((SelectOption)event.getSelectedOptions().get(0)).getLabel()))
         .setEphemeral(true)
         .queue(interaction -> event.getMessage().delete().queue(), error -> log.error("Error setting language", error));
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
            URL url = URI.create(avatarUrl).toURL();
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

   private String getString(Long guildId, String key, Object... args) {
      return this.bot.getSettingsManager().getLanguageService(guildId).getMessage(key, args);
   }
}
