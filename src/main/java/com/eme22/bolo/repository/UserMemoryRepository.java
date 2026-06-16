package com.eme22.bolo.repository;

import com.eme22.bolo.model.UserMemory;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class UserMemoryRepository implements PanacheRepository<UserMemory> {

    @Transactional
    public List<UserMemory> findActiveMemoriesForUser(Long guildId, Long targetUserId) {
        return list("guildId = ?1 and targetUserId = ?2 order by createdAt asc", guildId, targetUserId);
    }

    @Transactional
    public void deleteMemory(Long memoryId) {
        delete("id = ?1", memoryId);
    }

    @Transactional
    public void saveMemory2(UserMemory memory) {
        persist(memory);
    }

    @Transactional
    public void saveMemoryWithLimit(UserMemory memory, int maxLimit) {
        List<UserMemory> existing = findActiveMemoriesForUser(memory.getGuildId(), memory.getTargetUserId());
        if (existing.size() >= maxLimit) {
            int toDeleteCount = existing.size() - maxLimit + 1;
            for (int i = 0; i < toDeleteCount; i++) {
                deleteMemory(existing.get(i).getId());
            }
        }
        persist(memory);
    }
}
