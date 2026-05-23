package com.eme22.bolo.model;

import java.io.Serializable;
import java.util.Objects;

public class ServerStatId implements Serializable {
    private Long serverId;
    private String statName;

    public ServerStatId() {
    }

    public ServerStatId(Long serverId, String statName) {
        this.serverId = serverId;
        this.statName = statName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerStatId that = (ServerStatId) o;
        return Objects.equals(serverId, that.serverId) && Objects.equals(statName, that.statName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverId, statName);
    }
}
