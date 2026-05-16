package com.eme22.bolo.commands;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import com.jagrosh.jdautilities.command.Command.Category;
@Transactional
@ActivateRequestContext
public abstract class OwnerCommand extends BaseCommand {
   public OwnerCommand() {
      this.category = new Category("Owner");
      this.ownerCommand = true;
   }
}





