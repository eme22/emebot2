package com.eme22.bolo.entities;

import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;
import javax.swing.JOptionPane;
import lombok.Generated;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class Prompt {
   
   @ConfigProperty(name = "config.nogui-title")
   String title;
   @ConfigProperty(name = "config.nogui-message")
   String noguiMessage;
   private boolean nogui;
   private final boolean noprompt;
   private Scanner scanner;

   public Prompt() {
      this.noguiMessage = this.noguiMessage == null
         ? "Switching to nogui mode. You can manually start in nogui mode by including the -Dnogui=true flag."
         : this.noguiMessage;
      this.nogui = "true".equalsIgnoreCase(System.getProperty("nogui"));
      this.noprompt = "true".equalsIgnoreCase(System.getProperty("noprompt"));
   }

   public boolean isNoGUI() {
      return this.nogui;
   }

   public void alert(Prompt.Level level, String context, String message) {
      if (this.nogui) {
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
      } else {
         try {
            int option = 0;

            JOptionPane.showMessageDialog(null, "<html><body><p style='width: 400px;'>" + message, this.title, switch (level) {
               case INFO -> 1;
               case WARNING -> 2;
               case ERROR -> 0;
               default -> -1;
            });
         } catch (Exception var5) {
            this.nogui = true;
            this.alert(Prompt.Level.WARNING, context, this.noguiMessage);
            this.alert(level, context, message);
         }
      }
   }

   public String prompt(String content) {
      if (this.noprompt) {
         return null;
      } else if (this.nogui) {
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
      } else {
         try {
            return JOptionPane.showInputDialog(null, content, this.title, 3);
         } catch (Exception var4) {
            this.nogui = true;
            this.alert(Prompt.Level.WARNING, this.title, this.noguiMessage);
            return this.prompt(content);
         }
      }
   }

   public static enum Level {
      INFO,
      WARNING,
      ERROR;
   }
}
