package com.eme22.bolo.repository;

import com.eme22.bolo.model.AIGlobalConfig;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AIGlobalConfigRepository implements PanacheRepository<AIGlobalConfig> {

    public String getValue(String key) {
        AIGlobalConfig config = find("configKey = ?1", key).firstResult();
        return config != null ? config.getConfigValue() : null;
    }

    @Transactional
    public void setValue(String key, String value) {
        AIGlobalConfig config = find("configKey = ?1", key).firstResult();
        if (config == null) {
            config = AIGlobalConfig.builder()
                    .configKey(key)
                    .configValue(value)
                    .build();
            persist(config);
        } else {
            config.setConfigValue(value);
            persist(config);
        }
    }

    @Transactional
    public void deleteValue(String key) {
        delete("configKey = ?1", key);
    }
}
