package com.eme22.bolo.repository;

import com.eme22.bolo.model.ServerAIBackupConfig;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ServerAIBackupConfigRepository implements PanacheRepository<ServerAIBackupConfig> {

    public List<ServerAIBackupConfig> findByServerId(Long serverId) {
        return list("serverId = ?1 ORDER BY backupIndex ASC", serverId);
    }

    public Optional<ServerAIBackupConfig> findByServerIdAndIndex(Long serverId, int backupIndex) {
        return find("serverId = ?1 AND backupIndex = ?2", serverId, backupIndex).firstResultOptional();
    }

    @Transactional
    public void deleteByServerIdAndIndex(Long serverId, int backupIndex) {
        delete("serverId = ?1 AND backupIndex = ?2", serverId, backupIndex);
    }
}
