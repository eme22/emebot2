package com.eme22.bolo.configuration;

import io.smallrye.config.ConfigMapping;
import java.util.List;

@ConfigMapping(prefix = "lavalink")
public interface LavalinkProperties {
   List<Server> servers();

   interface Server {
      String name();
      String host();
      int port();
      String password();
      String region();
      boolean secure();
      int timeout();
   }
}

