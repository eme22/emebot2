package com.eme22.bolo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "ServerAIBackupConfig")
@Table(name = "embot_server_ai_backup_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerAIBackupConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "server_id", nullable = false)
    private Long serverId;

    @Column(name = "backup_index", nullable = false)
    private int backupIndex;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "model")
    private String model;
}
