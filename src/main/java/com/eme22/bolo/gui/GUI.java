package com.eme22.bolo.gui;

import com.eme22.bolo.Bot;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
public class GUI extends JFrame {
   private final ConsolePanel console;
   private final Bot bot;

   public GUI(Bot bot) {
      this.bot = bot;
      this.console = new ConsolePanel();
   }

   public static void setupHeadlessMode() {
   }

   public void init() {
      this.setDefaultCloseOperation(3);
      this.setTitle("Sentinel");
      JTabbedPane tabs = new JTabbedPane();
      tabs.add("Console", this.console);
      this.getContentPane().add(tabs);
      this.pack();
      this.setLocationRelativeTo(null);
      this.setVisible(true);
      //this.bot.setGUI(this);
      this.addWindowListener(new WindowListener() {
         @Override
         public void windowOpened(WindowEvent e) {
         }

         @Override
         public void windowClosing(WindowEvent e) {
            try {
               GUI.this.bot.shutdown();
            } catch (Exception var3) {
               System.exit(0);
            }
         }

         @Override
         public void windowClosed(WindowEvent e) {
         }

         @Override
         public void windowIconified(WindowEvent e) {
         }

         @Override
         public void windowDeiconified(WindowEvent e) {
         }

         @Override
         public void windowActivated(WindowEvent e) {
         }

         @Override
         public void windowDeactivated(WindowEvent e) {
         }
      });
   }
}
