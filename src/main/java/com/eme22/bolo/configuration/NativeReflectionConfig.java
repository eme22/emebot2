package com.eme22.bolo.configuration;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Configuration for Native Image reflection.
 * Register classes that are accessed via reflection here.
 */
@RegisterForReflection(targets = {
    com.eme22.bolo.utils.OtherUtil.class
})
public class NativeReflectionConfig {
}
