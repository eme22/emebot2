package com.eme22.bolo.commands.music;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import com.eme22.bolo.Bot;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Singleton;

@Singleton
@Transactional
@ActivateRequestContext
public class SCSearchCmd extends SearchCmd {
   @ConfigProperty(name = "config.aliases.scsearch", defaultValue = "")
   String[] aliases = new String[0];

   public SCSearchCmd(Bot bot) {
      super(bot);
      this.searchPrefix = "scsearch:";
      this.name = "scsearch";
      this.help = "searches Soundcloud for a provided query";
   }
}







