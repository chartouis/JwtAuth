package com.chitas.example.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.chitas.example.model.Fingerprint;
import com.chitas.example.model.User;

@Repository
public interface FingerprintsRepo extends JpaRepository<Fingerprint, Long> {
    Optional<User> findUserByHash(String hash);
    Optional<Fingerprint> findFingerprintByHash(String hash);
    boolean existsByHash(String hash);
}
