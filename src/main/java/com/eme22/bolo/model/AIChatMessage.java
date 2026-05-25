package com.eme22.bolo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity(name = "AIChatMessage")
@Table(name = "embot_ai_chat_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id", nullable = false)
    private Long id;

    @Column(name = "guild_id", nullable = false)
    private Long guildId;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "role", nullable = false)
    private String role;

    @Lob
    @Column(name = "content", length = 65535)
    private String content;

    @Column(name = "tool_call_id")
    private String toolCallId;

    @Column(name = "tool_name")
    private String toolName;

    @Column(name = "message_timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "discord_message_id")
    private Long discordMessageId;
}
