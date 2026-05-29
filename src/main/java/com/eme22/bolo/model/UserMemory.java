package com.eme22.bolo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity(name = "UserMemory")
@Table(name = "embot_user_memories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "guild_id", nullable = false)
    private Long guildId;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(name = "memory_text", nullable = false, length = 1000)
    private String memoryText;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
