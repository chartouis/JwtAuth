package com.chitas.example.service;

import java.security.SecureRandom;
import java.time.Instant;

import org.springframework.stereotype.Service;
import lombok.extern.log4j.Log4j2;

import com.chitas.example.model.FACode;
import com.chitas.example.model.Fingerprint;
import com.chitas.example.model.User;
import com.chitas.example.repo.CodesRepo;
import com.chitas.example.repo.FingerprintsRepo;
import com.chitas.example.repo.UsersRepo;

@Service
@Log4j2
public class TwoFactorService {
    private final FingerprintsRepo fRepo;
    private final CodesRepo cRepo;

    public TwoFactorService(FingerprintsRepo fRepo, CodesRepo cRepo, UsersRepo uRepo) {
        this.fRepo = fRepo;
        this.cRepo = cRepo;
    }

    public boolean fingerprintExists(String hash) {
        boolean exists = fRepo.existsByHash(hash);
        log.info("Fingerprint exists for hash {}: {}", hash, exists);
        return exists;
    }

    public User getFingerprintUser(Fingerprint fingerprint) {
        if (!fingerprintExists(fingerprint.getHash())) {
            log.warn("No user found for fingerprint hash: {}", fingerprint.getHash());
            return null;
        }
        User user = fRepo.findUserByHash(fingerprint.getHash());
        log.info("Found user for fingerprint hash {}: {}", fingerprint.getHash(), user);
        return user;
    }

    public Fingerprint getFingerprintByHash(String hash) {
        Fingerprint fingerprint = fRepo.findFingerprintByHash(hash);
        if (fingerprint == null) {
            log.warn("No fingerprint found for hash: {}", hash);
        } else {
            log.info("Fingerprint found for hash {}: {}", hash, fingerprint);
        }
        return fingerprint;
    }

    private FACode saveCode(FACode fCode) {
        log.info("Saving code: {}", fCode);
        return cRepo.save(fCode);
    }

    public FACode generateCode(Fingerprint fingerprint) {
        FACode code = new FACode(generateRandomSixDigitString(), generateExpiration(), fingerprint);
        log.info("Generated code: {}", code);
        return saveCode(code);
    }

    private static String generateRandomSixDigitString() {
        SecureRandom random = new SecureRandom();
        int number = 100_000 + random.nextInt(900_000);
        String code = String.valueOf(number);
        log.debug("Generated random code: {}", code);
        return code;
    }

    public static final int EXPIRATION_IN_SECONDS = 60 * 5;

    private static Instant generateExpiration() {
        Instant expiration = Instant.now().plusSeconds(EXPIRATION_IN_SECONDS);
        log.debug("Generated expiration time: {}", expiration);
        return expiration;
    }

    public boolean fingerprintAndCodeMatch(Fingerprint fingerprint, FACode code) {
        boolean match = fingerprint.getId().equals(code.getFingerprint().getId());
        log.info("Fingerprint and code match: {}", match);
        return match;
    }

    public boolean verifyFingerprint(String code, String hash) {
        FACode fcode = cRepo.findFACodeByCode(code);
        if (fcode == null) {
            log.error("Invalid code: {}", code);
            return false;
        }

        Fingerprint submitted = fRepo.findFingerprintByHash(hash);
        if (submitted == null) {
            log.warn("No fingerprint found for hash: {}", hash);
            return false;
        }

        Fingerprint expected = fcode.getFingerprint();
        if (expected == null) {
            log.warn("Expected fingerprint not found for code: {}", code);
            return false;
        }

        if (!submitted.getHash().equals(expected.getHash())) {
            log.warn("Fingerprint hash mismatch for code: {}", code);
            return false;
        }

        expected.setVerified(true);
        fRepo.save(expected);
        log.info("Fingerprint verified successfully for code: {}", code);
        return true;
    }

    public User getUserbyCode(String code) {
        FACode fcode = cRepo.findFACodeByCode(code);
        if (fcode == null) {
            log.error("Invalid code: {}", code);
            return null;
        }
        Fingerprint f = fcode.getFingerprint();
        User user = f.getUser();
        log.info("Found user for code: {}", code);
        return user;
    }

    public Fingerprint createFingerprint(String hash, User user) {
        Fingerprint fingerprint = fRepo.save(new Fingerprint(hash, user));
        log.info("Created fingerprint for hash {}: {}", hash, fingerprint);
        return fingerprint;
    }
}
