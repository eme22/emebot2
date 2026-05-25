package com.eme22.bolo.services;

import com.eme22.bolo.model.UserOffense;
import com.eme22.bolo.repository.UserOffenseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;

@ApplicationScoped
@Transactional
public class UserOffenseService {

    @Inject
    UserOffenseRepository repository;

    public UserOffense getOrCreateOffenses(Long userId) {
        UserOffense offense = repository.findByUserId(userId);
        if (offense == null) {
            offense = UserOffense.builder()
                    .userId(userId)
                    .offenseCount(0)
                    .lastOffenseTimestamp(null)
                    .banUntil(null)
                    .build();
            repository.persist(offense);
        }
        return offense;
    }

    public UserOffense addOffense(Long userId) {
        UserOffense offense = getOrCreateOffenses(userId);
        offense.setOffenseCount(offense.getOffenseCount() + 1);
        offense.setLastOffenseTimestamp(Instant.now());

        if (offense.getOffenseCount() >= 5) {
            offense.setBanUntil(calculateBanUntil(offense.getOffenseCount()));
        }

        repository.persist(offense);
        return offense;
    }

    public boolean isBanned(Long userId) {
        UserOffense offense = repository.findByUserId(userId);
        if (offense == null || offense.getBanUntil() == null) {
            return false;
        }
        if (Instant.now().isAfter(offense.getBanUntil())) {
            return false;
        }
        return true;
    }

    public void clearOffenses(Long userId) {
        UserOffense offense = getOrCreateOffenses(userId);
        offense.setOffenseCount(0);
        offense.setLastOffenseTimestamp(null);
        offense.setBanUntil(null);
        repository.persist(offense);
    }

    public void setOffenseCount(Long userId, int offenses) {
        UserOffense offense = getOrCreateOffenses(userId);
        offense.setOffenseCount(offenses);
        offense.setLastOffenseTimestamp(Instant.now());

        if (offenses >= 5) {
            offense.setBanUntil(calculateBanUntil(offenses));
        } else {
            offense.setBanUntil(null);
        }
        repository.persist(offense);
    }

    public Instant calculateBanUntil(int offenseCount) {
        if (offenseCount < 5) {
            return null;
        }
        long seconds;
        switch (offenseCount) {
            case 5:
                seconds = 3600; // 1 hour
                break;
            case 6:
                seconds = 4 * 3600; // 4 hours
                break;
            case 7:
                seconds = 12 * 3600; // 12 hours
                break;
            case 8:
                seconds = 24 * 3600; // 1 day
                break;
            case 9:
                seconds = 3 * 24 * 3600; // 3 days
                break;
            default:
                seconds = 7 * 24 * 3600; // 7 days (1 week max)
                break;
        }
        return Instant.now().plusSeconds(seconds);
    }
}
