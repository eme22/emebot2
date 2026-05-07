package com.eme22.bolo.commands.admin;

import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;

public class DeleteChannelRequestCmd extends AdminCommand {
   public DeleteChannelRequestCmd(Category adminCategory) {
      super(adminCategory);
   }

   protected void execute(SlashCommandEvent event) {
   }
}
