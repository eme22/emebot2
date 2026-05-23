package com.eme22.bolo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity(name = "embot_server_stats")
@IdClass(ServerStatId.class)
public class ServerStat {
    @Id
    @Column(name = "server_id", nullable = false)
    private Long serverId;

    @Id
    @Column(name = "stat_name", nullable = false)
    private String statName;

    @Column(name = "stat_value", nullable = false)
    private Long value;

    public ServerStat() {
    }

    public ServerStat(Long serverId, String statName, Long value) {
        this.serverId = serverId;
        this.statName = statName;
        this.value = value;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public String getStatName() {
        return statName;
    }

    public void setStatName(String statName) {
        this.statName = statName;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
