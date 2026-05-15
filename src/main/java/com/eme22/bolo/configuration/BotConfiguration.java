package com.eme22.bolo.configuration;

import lombok.extern.slf4j.Slf4j;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.owner.EvalCmd;
import com.eme22.bolo.entities.Prompt;
import com.eme22.bolo.listeners.CommandLogListener;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.settings.SettingsManager;
import com.eme22.imageapi.AnimeImageClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.GuildSettingsManager;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.UserContextMenu;
import com.jagrosh.jdautilities.command.MessageContextMenu;
import com.jagrosh.jdautilities.command.Command.Category;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.NodeOptions.Builder;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Generated;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class BotConfiguration {
   
   public static final Permission[] RECOMMENDED_PERMS = new Permission[]{
      Permission.MESSAGE_HISTORY,
      Permission.MESSAGE_ADD_REACTION,
      Permission.MESSAGE_EMBED_LINKS,
      Permission.MESSAGE_ATTACH_FILES,
      Permission.MESSAGE_MANAGE,
      Permission.ADMINISTRATOR,
      Permission.MESSAGE_EXT_EMOJI,
      Permission.MANAGE_CHANNEL,
      Permission.VOICE_CONNECT,
      Permission.VOICE_SPEAK,
      Permission.NICKNAME_CHANGE,
      Permission.MANAGE_WEBHOOKS
   };
   public static final GatewayIntent[] INTENTS = new GatewayIntent[]{
      GatewayIntent.DIRECT_MESSAGES,
      GatewayIntent.MESSAGE_CONTENT,
      GatewayIntent.GUILD_MESSAGES,
      GatewayIntent.GUILD_MESSAGE_REACTIONS,
      GatewayIntent.GUILD_VOICE_STATES,
      GatewayIntent.GUILD_MEMBERS,
      GatewayIntent.GUILD_WEBHOOKS
   };
   public static final String message = "Hola soy MBot un BOT con lag) (v%s) ";
   private static final String DEFAULT_PREFIX = "@mention";
   public static final String[] features = new String[]{
      "Musica en HQ", "Mensaje de bienvenida y despedida configurables", "Limpiar mensajes", "Votaciones", "Memes", "Manejo de roles"
   };
   public static final int DEFAULT_VOLUME = 100;
   private final Prompt prompt;
   private static final String CONTEXT = "Config";
   private static final String START_TOKEN = "/// START OF JMUSICBOT CONFIG ///";
   private static final String END_TOKEN = "/// END OF JMUSICBOT CONFIG ///";
   @ConfigProperty(name = "jda.token")
   String token;
   @ConfigProperty(name = "jda.shardId", defaultValue = "0")
   int shardId;
   @ConfigProperty(name = "jda.totalShards", defaultValue = "1")
   int totalShards;
   @ConfigProperty(name = "config.prefix")
   String prefix;
   @ConfigProperty(name = "config.altprefix")
   String altprefix;
   @ConfigProperty(name = "config.help")
   String helpWord;
   @ConfigProperty(name = "config.playlistsfolder")
   String playlistsFolder;
   @ConfigProperty(name = "config.success")
   String successEmoji;
   @ConfigProperty(name = "config.warning")
   String warningEmoji;
   @ConfigProperty(name = "config.error")
   String errorEmoji;
   @ConfigProperty(name = "config.loading")
   String loadingEmoji;
   @ConfigProperty(name = "config.searching")
   String searchingEmoji;
   @ConfigProperty(name = "config.welcome")
   String welcomeString;
   @ConfigProperty(name = "config.goodbye")
   String goodByeString;
   @ConfigProperty(name = "config.stayinchannel")
   boolean stayInChannel;
   @ConfigProperty(name = "config.songinstatus")
   boolean songInStatus;
   @ConfigProperty(name = "config.nowplayingimages")
   boolean npImages;
   @ConfigProperty(name = "config.update")
   boolean updatealerts;
   @ConfigProperty(name = "config.eval")
   boolean useEval;
   private boolean dbots;
   @ConfigProperty(name = "config.owner")
   long owner;
   @ConfigProperty(name = "config.maxseconds")
   long maxSeconds;
   @ConfigProperty(name = "config.alonetimeuntilstop")
   long aloneTimeUntilStop;
   private OnlineStatus status;
   @ConfigProperty(name = "config.game")
   String game;
   @ConfigProperty(name = "config.discordChannel", defaultValue = "")
   String botDiscord;
   private final SettingsManager settingsManager;

   @Inject
   public BotConfiguration(Prompt prompt, SettingsManager settingsManager) {
      this.prompt = prompt;
      this.settingsManager = settingsManager;
   }

   @Produces
   @Singleton
   JDA getJDA(CommandClientBuilder cb, Activity game, EventWaiter waiter, Bot bot, LavalinkClient client, jakarta.enterprise.inject.Instance<net.dv8tion.jda.api.hooks.EventListener> listeners) {
      boolean nogame = false;
      if (this.status != OnlineStatus.UNKNOWN) {
         cb.setStatus(this.status);
      }

      if (game == null) {
         cb.useDefaultGame();
      } else if (game.getName().equalsIgnoreCase("none")) {
         cb.setActivity(null);
         nogame = true;
      } else {
         cb.setActivity(game);
      }

      java.util.List<Object> allListeners = new java.util.ArrayList<>();
      allListeners.add(cb.build());
      allListeners.add(waiter);

      for (net.dv8tion.jda.api.hooks.EventListener listener : listeners) {
          if (!allListeners.contains(listener)) {
              allListeners.add(listener);
          }
      }

      log.info("Adding {} total listeners to JDA", allListeners.size());

      log.info("Starting JDA with Shard {} of {}", this.shardId, this.totalShards);

      JDA jda = JDABuilder.create(this.token, Arrays.asList(INTENTS))
         .useSharding(this.shardId, this.totalShards)
         .enableCache(CacheFlag.MEMBER_OVERRIDES, new CacheFlag[]{CacheFlag.VOICE_STATE})
         .disableCache(CacheFlag.ACTIVITY, new CacheFlag[]{CacheFlag.CLIENT_STATUS, CacheFlag.EMOJI, CacheFlag.ONLINE_STATUS})
         .setActivity(nogame ? null : Activity.playing("loading..."))
         .setStatus(this.status != OnlineStatus.INVISIBLE && this.status != OnlineStatus.OFFLINE ? OnlineStatus.DO_NOT_DISTURB : OnlineStatus.INVISIBLE)
         .setBulkDeleteSplittingEnabled(true)
         .addEventListeners(allListeners.toArray())
         .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(client))
         .build();
      try {
         jda.awaitReady();
      } catch (InterruptedException e) {
         throw new RuntimeException("JDA login interrupted", e);
      }
      if (jda.getStatus() != net.dv8tion.jda.api.JDA.Status.CONNECTED) {
         throw new RuntimeException("JDA failed to login. Status: " + jda.getStatus());
      }
      bot.setJDA(jda);
      return jda;
   }

   @Produces
   @Singleton
   EventWaiter getEventWaiter() {
      return new EventWaiter();
   }

   @Produces
   @Singleton
   CommandClientBuilder getCommandClientBuilder(
      GuildSettingsManager<Server> setting,
      Activity game,
      Bot bot,
      Prompt prompt,
      Consumer<CommandEvent> helpbean,
      jakarta.enterprise.inject.Instance<Command> commands,
      jakarta.enterprise.inject.Instance<SlashCommand> slashCommands,
      CommandLogListener commandLog,
      jakarta.enterprise.inject.Instance<UserContextMenu> userContextMenus,
      jakarta.enterprise.inject.Instance<MessageContextMenu> messageContextMenus
   ) {
      CommandClientBuilder cb = new CommandClientBuilder()
         .setPrefix(this.prefix)
         .setAlternativePrefix(this.altprefix)
         .setOwnerId(Long.toString(this.owner))
         .setEmojis(this.successEmoji, this.warningEmoji, this.errorEmoji)
         .useHelpBuilder(false)
         .setHelpWord(this.helpWord)
         .setHelpConsumer(helpbean)
         .setLinkedCacheSize(200)
         .setGuildSettingsManager(setting)
         .addSlashCommands(slashCommands.stream().filter(Objects::nonNull).toArray(SlashCommand[]::new))
         .addCommands(commands.stream().filter(Objects::nonNull).toArray(Command[]::new))
         .addContextMenus(userContextMenus.stream().filter(Objects::nonNull).toArray(UserContextMenu[]::new))
         .addContextMenus(messageContextMenus.stream().filter(Objects::nonNull).toArray(MessageContextMenu[]::new))
         .setListener(commandLog);
      if (this.status != OnlineStatus.UNKNOWN) {
         cb.setStatus(this.status);
      }

      if (game == null) {
         cb.useDefaultGame();
      } else if (game.getName().equalsIgnoreCase("none")) {
         cb.setActivity(null);
      } else {
         cb.setActivity(game);
      }

      return cb;
   }

   @Produces
   @Singleton
   Command getEvalCommand(Bot bot) {
      return this.useEval ? new EvalCmd(bot) : null;
   }



   @Produces
   @Singleton
   public Activity activityBean() {
      return Activity.playing(this.game);
   }

   @Produces
   @Singleton
   @jakarta.inject.Named("djCategory")
   public Category djCategoryBean() {
      return new Category("DJ", event -> {
         if (event.getAuthor().getId().equals(event.getClient().getOwnerId())) {
            return true;
         } else if (event.getAuthor().equals(event.getGuild().getOwner().getUser())) {
            return true;
         } else {
            Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
            Role admin = event.getGuild().getRoleById(settings.getAdminRoleId());
            if (event.getMember().getRoles().contains(admin)) {
               return true;
            } else {
               Role dj = event.getGuild().getRoleById(settings.getDjRoleId());
               return dj != null && (event.getMember().getRoles().contains(dj) || dj.getIdLong() == event.getMember().getIdLong());
            }
         }
      });
   }

   @Produces
   @Singleton
   @jakarta.inject.Named("adminCategory")
   public Category adminCategoryBean() {
      return new Category("Admin", event -> {
         if (event.getAuthor().getId().equals(event.getClient().getOwnerId())) {
            log.info("Owner is executing command {}", event.getMessage().getContentRaw());
            return true;
         } else if (event.getAuthor().getId().equals(event.getGuild().getOwnerId())) {
            log.info("Guild owner is executing command {}", event.getMessage().getContentRaw());
            return true;
         } else if (event.getGuild() == null) {
            log.info("Guild is null, returning true");
            return true;
         } else {
            Server settings = (Server)event.getClient().getSettingsFor(event.getGuild());
            Role admin = event.getGuild().getRoleById(settings.getAdminRoleId());
            boolean isAdminNotNull = admin != null;
            Boolean isAdmin = event.getMember().getRoles().contains(admin);
            Boolean isOwner = admin.getIdLong() == event.getGuild().getIdLong();
            log.info("Evaluating command for admin category. Admin is not null: {}, isAdmin: {}, isOwner: {}", isAdminNotNull, isAdmin, isOwner);
            boolean result = isAdminNotNull && (isAdmin || isOwner);
            log.info("Result: {}", result);
            return result;
         }
      });
   }

   @Produces
   @Singleton
   public Consumer<CommandEvent> helpBean(jakarta.enterprise.inject.Instance<Command> commands) {
      return event -> {
         StringBuilder builder = new StringBuilder("Comandos de **" + event.getSelfUser().getName() + "**\n");
         List<String> strings = new ArrayList<>();
         int preMaxSize = 1980;
         Category category = null;

         for (Command command : commands) {
            if (!command.isHidden() && (!command.isOwnerCommand() || event.isOwner())) {
               if (!Objects.equals(category, command.getCategory())) {
                  category = command.getCategory();
                  builder.append("\n\n  __").append(category == null ? "Miscelaneos" : category.getName()).append("__:\n");
               }

               builder.append("\n`")
                  .append(this.prefix.equals("@mention") ? "@" + event.getJDA().getSelfUser().getName() + " " : this.prefix)
                  .append(this.prefix == null ? " " : "")
                  .append(command.getName())
                  .append(command.getArguments() == null ? "`" : " " + command.getArguments() + "`")
                  .append(" - ")
                  .append(command.getHelp());
               if (builder.length() > preMaxSize) {
                  strings.add(builder.toString());
                  builder = new StringBuilder();
               }
            }
         }

         log.error("Owner: " + this.owner);
         User owner2 = event.getJDA().getUserById(this.owner);
         log.error("Owner2: " + owner2);
         if (owner2 != null) {
            if (builder.length() > preMaxSize) {
               strings.add(builder.toString());
               builder = new StringBuilder();
            }

            builder.append("\n\nPara ayuda adicional, contacta a **").append(owner2.getName()).append("**#").append(owner2.getDiscriminator());
            if (this.botDiscord != null && !this.botDiscord.isEmpty()) {
               builder.append(" o unete a ").append(this.botDiscord);
            }

            strings.add(builder.toString());
         }

         for (String message : strings) {
            event.replyInDm(message, unused -> {}, t -> event.replyWarning("Help cannot be sent because you are blocking Direct Messages."));
         }
      };
   }

   @Produces
   @Singleton
   AnimeImageClient getAnimeImageClient() {
      return new AnimeImageClient();
   }

   @Produces
   @Singleton
   LavalinkClient getLavalink(LavalinkProperties properties) {
      LavalinkClient client = new LavalinkClient(Helpers.getUserIdFromToken(this.token));
      this.registerLavalinkNodes(client, properties);
      return client;
   }


   private void registerLavalinkNodes(LavalinkClient client, LavalinkProperties properties) {
      properties.servers()
         .forEach(
            s -> {
               Builder options = new Builder()
                  .setName(s.name())
                  .setPassword(s.password())
                  .setServerUri(URI.create((s.secure() ? "wss://" : "ws://") + s.host() + ":" + s.port() + "/"))
                  .setHttpTimeout(s.timeout());
               client.addNode(options.build());
            }
         );
   }
}

