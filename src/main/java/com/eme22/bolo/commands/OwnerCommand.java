package com.eme22.bolo.commands;

import com.jagrosh.jdautilities.command.Command.Category;

public abstract class OwnerCommand extends BaseCommand {
   public OwnerCommand() {
      this.category = new Category("Owner");
      this.ownerCommand = true;
   }
}
