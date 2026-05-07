package com.eme22.bolo.configuration;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Configuration for Native Image reflection.
 * Register classes that are accessed via reflection here.
 */
@RegisterForReflection(targets = {
    // JDA and Lavalink classes might need reflection.
    // If you encounter ClassNotFoundException or similar in native mode, add the classes here.
})
public class NativeReflectionConfig {
}
