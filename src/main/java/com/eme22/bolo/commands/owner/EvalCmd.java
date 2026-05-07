package com.eme22.bolo.commands.owner;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.Collections;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
public class EvalCmd extends OwnerCommand {
   private final Bot bot;
   private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
   @ConfigProperty(name = "config.aliases.eval", defaultValue = "")
   String[] aliases = new String[0];

   public EvalCmd(@NotNull Bot bot) {
      this.bot = bot;
      this.name = "eval";
      this.help = "evaluates nashorn code";
      this.guildOnly = false;
      this.options = Collections.singletonList(new OptionData(OptionType.STRING, "code", "Eval Code").setRequired(true));
   }

   protected void execute(SlashCommandEvent event) {
      long startTime = System.currentTimeMillis();
      ScheduledFuture<?> scheduleCancel = scheduler.schedule(cancelDeferred(event), 17L, TimeUnit.SECONDS);
      ScheduledFuture<?> schedule = scheduler.schedule(generateTimerTask(event), 3L, TimeUnit.SECONDS);
      String command = (String)event.getOption("code", OptionMapping::getAsString);
      String reply = null;
      ScriptEngine se = this.setupDefaultEngine(event);

      try {
         reply = se.eval(command).toString();
      } catch (ScriptException var10) {
         event.reply(event.getClient().getError() + " An exception was thrown:\n```\n" + var10 + " ```").queue();
         schedule.cancel(true);
         scheduleCancel.cancel(true);
         return;
      }

      if (System.currentTimeMillis() - startTime < 3000L) {
         event.reply(event.getClient().getSuccess() + " Evaluated Successfully:\n```\n" + reply + " ```").queue();
         schedule.cancel(true);
         scheduleCancel.cancel(true);
      } else {
         event.reply(event.getClient().getSuccess() + " Evaluated Successfully:\n```\n" + reply + " ```").queue();
         schedule.cancel(false);
         scheduleCancel.cancel(true);
      }
   }

   @NotNull
   private static TimerTask generateTimerTask(SlashCommandEvent event) {
      return new TimerTask() {
         @Override
         public void run() {
            event.deferReply().complete();
         }
      };
   }

   @NotNull
   private static TimerTask cancelDeferred(SlashCommandEvent event) {
      return new TimerTask() {
         @Override
         public void run() {
            event.reply(event.getClient().getError() + " A timeout has ocurred").queue();
         }
      };
   }

   protected void execute(CommandEvent event) {
      String command = event.getArgs();
      ScriptEngine se = this.setupDefaultEngine(event);

      try {
         event.reply(event.getClient().getSuccess() + " Evaluated Successfully:\n```\n" + se.eval(command) + " ```");
      } catch (Exception var5) {
         event.reply(event.getClient().getError() + " An exception was thrown:\n```\n" + var5 + " ```");
      }
   }

   @NotNull
   private ScriptEngine setupDefaultEngine(SlashCommandEvent event) {
      return this.setupDefaultEngine(event, null);
   }

   @NotNull
   private ScriptEngine setupDefaultEngine(CommandEvent event) {
      return this.setupDefaultEngine(null, event);
   }

   @NotNull
   private ScriptEngine setupDefaultEngine(SlashCommandEvent event1, CommandEvent event2) {
      ScriptEngine se = new ScriptEngineManager().getEngineByName("Nashorn");
      se.put("bot", this.bot);
      se.put("event", event1 == null ? event2 : event1);
      se.put("jda", event1 == null ? event2.getJDA() : event1.getJDA());
      se.put("guild", event1 == null ? event2.getGuild() : event1.getGuild());
      se.put("channel", event1 == null ? event2.getTextChannel() : event1.getTextChannel());
      return se;
   }
}


