package com.chitas.example.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chitas.example.model.FACode;
import com.chitas.example.model.Fingerprint;

@Repository
public interface CodesRepo extends JpaRepository<FACode, Long> {
    @Query("SELECT f.fingerprint FROM FACode f WHERE f.id = :facodeId")
    Optional<Fingerprint> findFingerprintByFacodeId(@Param("facodeId") Long facodeId);

    public Optional<FACode> findFACodeByCode(String code);
    public boolean existsByCode(String code);
}