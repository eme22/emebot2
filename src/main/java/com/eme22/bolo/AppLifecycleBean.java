package com.eme22.bolo;

import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.JDA;
import lombok.extern.slf4j.Slf4j;

@Startup
@ApplicationScoped
@Slf4j
public class AppLifecycleBean {

    @Inject
    JDA jda;

    @Inject
    Bot bot;

    void onStart(@Observes StartupEvent ev) {
        log.info("The application is starting...");
        log.info("JDA Status: {}", jda.getStatus());
    }
}
