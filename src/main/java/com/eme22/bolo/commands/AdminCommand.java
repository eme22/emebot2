package com.eme22.bolo.commands;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import com.jagrosh.jdautilities.command.Command.Category;
import net.dv8tion.jda.api.Permission;
import jakarta.inject.Inject;
import jakarta.inject.Named;
@Transactional
@ActivateRequestContext
public abstract class AdminCommand extends BaseCommand {
   @Inject
   public AdminCommand(@Named("adminCategory") Category adminCategory) {
      this.category = adminCategory;
      this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
   }
}





