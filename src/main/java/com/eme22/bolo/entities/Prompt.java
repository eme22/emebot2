package com.eme22.bolo.entities;

import lombok.extern.slf4j.Slf4j;
import java.util.Scanner;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class Prompt {
   
   @ConfigProperty(name = "config.nogui-title")
   String title;
   @ConfigProperty(name = "config.nogui-message")
   String noguiMessage;
   private final boolean noprompt;
   private Scanner scanner;

   public Prompt() {
      this.noguiMessage = this.noguiMessage == null
         ? "Switching to nogui mode. You can manually start in nogui mode by including the -Dnogui=true flag."
         : this.noguiMessage;
      this.noprompt = "true".equalsIgnoreCase(System.getProperty("noprompt"));
   }

   public boolean isNoGUI() {
      return true;
   }

   public void alert(Prompt.Level level, String context, String message) {
      switch (level) {
         case INFO:
            log.info(message);
            break;
         case WARNING:
            log.warn(message);
            break;
         case ERROR:
            log.error(message);
            break;
         default:
            log.info(message);
      }
   }

   public String prompt(String content) {
      if (this.noprompt) {
         return null;
      }
      
      if (this.scanner == null) {
         this.scanner = new Scanner(System.in);
      }

      try {
         System.out.println(content);
         return this.scanner.hasNextLine() ? this.scanner.nextLine() : null;
      } catch (Exception var3) {
         this.alert(Prompt.Level.ERROR, this.title, "Unable to read input from command line.");
         var3.printStackTrace();
         return null;
      }
   }

   public static enum Level {
      INFO,
      WARNING,
      ERROR;
   }
}
