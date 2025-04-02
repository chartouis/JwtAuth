package com.chitas.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chitas.example.model.FACode;
import com.chitas.example.model.Fingerprint;

@Repository
public interface CodesRepo extends JpaRepository<FACode, Long> {
    public Fingerprint findFingerprintById(Long id);
}