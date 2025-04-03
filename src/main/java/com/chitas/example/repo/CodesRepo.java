package com.chitas.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chitas.example.model.FACode;
import com.chitas.example.model.Fingerprint;

@Repository
public interface CodesRepo extends JpaRepository<FACode, Long> {
    @Query("SELECT f.fingerprint FROM FACode f WHERE f.id = :facodeId")
    Fingerprint findFingerprintByFacodeId(@Param("facodeId") Long facodeId);

    public FACode findFACodeByCode(String code);
}