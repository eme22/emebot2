package com.eme22.bolo.configuration;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Configuration for Native Image reflection.
 * Register classes that are accessed via reflection here.
 */
@RegisterForReflection(
    targets = {
        com.eme22.bolo.utils.OtherUtil.class
    },
    classNames = {
        "com.eme22.discordcdn.model.RefreshedUrl",
        "com.eme22.discordcdn.model.RefreshUrlsRes",
        "com.eme22.discordcdn.model.ParsedLink",
        "com.eme22.discordcdn.model.LinkIssue",
        "com.eme22.discordcdn.model.LinkData",
        "com.eme22.discordcdn.Discord",
        "com.eme22.discordcdn.util.LinkParser"
    }
)
public class NativeReflectionConfig {
}
