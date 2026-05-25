package com.eme22.bolo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "AIGlobalConfig")
@Table(name = "embot_ai_global_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIGlobalConfig {

    @Id
    @Column(name = "config_key", nullable = false)
    private String configKey;

    @Column(name = "config_value", length = 4000)
    private String configValue;
}
