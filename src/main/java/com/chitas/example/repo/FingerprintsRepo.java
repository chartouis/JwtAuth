package com.chitas.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.chitas.example.model.Fingerprint;
import com.chitas.example.model.User;

@Repository
public interface FingerprintsRepo extends JpaRepository<Fingerprint, Long> {
    User findUserByHash(String hash);
    Fingerprint findFingerprintByHash(String hash);
    boolean existsByHash(String hash);
}
