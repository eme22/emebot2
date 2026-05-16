package com.eme22.bolo.commands.admin;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;
import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.Command.Category;
@Transactional
@ActivateRequestContext
public class EditChannelCmd extends AdminCommand {
   public EditChannelCmd(Category adminCategory) {
      super(adminCategory);
   }

   public void execute(SlashCommandEvent event) {
   }
}









