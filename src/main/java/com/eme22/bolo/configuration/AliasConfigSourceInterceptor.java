package com.eme22.bolo.configuration;

import io.smallrye.config.ConfigSourceInterceptor;
import io.smallrye.config.ConfigSourceInterceptorContext;
import io.smallrye.config.ConfigValue;

public class AliasConfigSourceInterceptor implements ConfigSourceInterceptor {

    @Override
    public ConfigValue getValue(ConfigSourceInterceptorContext context, String name) {
        ConfigValue value = context.proceed(name);

        // If the property is missing and matches config.aliases.*, fall back to an empty string
        if (value == null && name != null && name.startsWith("config.aliases.")) {
            return ConfigValue.builder()
                .withName(name)
                .withValue("")
                .withConfigSourceName("default-aliases-fallback")
                .build();
        }

        return value;
    }
}
