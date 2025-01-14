package com.tl.reap_admin_api.repository;

import com.tl.reap_admin_api.model.TraineeCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TraineeRepository extends JpaRepository<TraineeCredential, Long> {
    Optional<TraineeCredential> findByUsername(String username);
    Optional<TraineeCredential> findByUuid(UUID uuid);
}