package com.eme22.bolo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity(name = "UserOffense")
@Table(name = "embot_user_offenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOffense {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "offense_count", nullable = false)
    private int offenseCount;

    @Column(name = "last_offense_timestamp")
    private Instant lastOffenseTimestamp;

    @Column(name = "ban_until")
    private Instant banUntil;
}
