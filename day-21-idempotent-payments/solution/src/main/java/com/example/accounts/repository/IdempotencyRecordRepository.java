package com.example.accounts.repository;

import com.example.accounts.model.IdempotencyRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByUsernameAndIdempotencyKey(String username, String idempotencyKey);
}
