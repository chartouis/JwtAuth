package com.chitas.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chitas.example.model.FACode;
import com.chitas.example.model.Fingerprint;
import com.chitas.example.model.User;

@Repository
interface FingerprintsRepo extends JpaRepository<Long, Fingerprint> {
    public User findUserById(Long fingerprintId);
} 

@Repository
interface CodesRepo extends JpaRepository<Long, FACode> {
    public Fingerprint findFingerprintById(Long id);
    
}