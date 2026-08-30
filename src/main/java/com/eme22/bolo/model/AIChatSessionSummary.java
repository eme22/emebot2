package com.eme22.bolo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity(name = "AIChatSessionSummary")
@Table(name = "embot_ai_chat_session_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIChatSessionSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id", nullable = false)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId;

    @Lob
    @Column(name = "summary_text", length = 65535)
    private String summaryText;

    @Column(name = "summarized_up_to_message_id", nullable = false)
    private Long summarizedUpToMessageId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
