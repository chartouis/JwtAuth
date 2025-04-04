package com.chitas.example.service;

import java.security.SecureRandom;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.chitas.example.model.FACode;
import com.chitas.example.model.Fingerprint;
import com.chitas.example.model.User;
import com.chitas.example.model.Wrappers.CodeAndFingerprint;
import com.chitas.example.model.Wrappers.UserAndFingerPrint;
import com.chitas.example.repo.CodesRepo;
import com.chitas.example.repo.FingerprintsRepo;
import com.chitas.example.repo.UsersRepo;

@Service
public class TwoFactorService {
    private final FingerprintsRepo fRepo;
    private final CodesRepo cRepo;
    private final UsersRepo uRepo;

    public TwoFactorService(FingerprintsRepo fRepo, CodesRepo cRepo, UsersRepo uRepo) {
        this.fRepo = fRepo;
        this.cRepo = cRepo;
        this.uRepo = uRepo;

    }

    public boolean fingerprintExists(Fingerprint fingerprint) {
        if (fingerprint.getHash() == null) {
            return false;
        }
        return fRepo.existsByHash(fingerprint.getHash());

    }

    // public Fingerprint fromUAFtoFingerprint(UserAndFingerPrint uaf){
    // return new Fingerprint(Fin, null)
    // }

    public User getFingerprintUser(Fingerprint fingerprint) {
        if (!fingerprintExists(fingerprint)) {
            return null;
        }
        return fRepo.findUserByHash(fingerprint.getHash());
    }

    public Fingerprint getFingerprintByHash(String hash) {
        return fRepo.findFingerprintByHash(hash);
    }

    private FACode saveCode(FACode fCode) {
        return cRepo.save(fCode);
    }

    public FACode generateCode(Fingerprint fingerprint) {
        FACode code = new FACode(generateRandomSixDigitString(), generateExpiration(), fingerprint);
        return saveCode(code);
    }

    private static String generateRandomSixDigitString() {
        SecureRandom random = new SecureRandom();
        int number = 100_000 + random.nextInt(900_000); // Ensures 6 digits
        return String.valueOf(number);
    }

    public static final int EXPIRATION_IN_SECONDS = 60 * 5;

    private static Instant generateExpiration() {
        return Instant.now().plusSeconds(EXPIRATION_IN_SECONDS);
    }

    public boolean fingerprintAndCodeMatch(Fingerprint fingerprint, FACode code) {
        return fingerprint.getId().equals(code.getFingerprint().getId());
    }

    public boolean verifyFingerprint(CodeAndFingerprint caf) {
        FACode fcode = cRepo.findFACodeByCode(caf.getCode().getCode());
    
        if (fcode == null) {
            System.out.println("Invalid code");
            return false;
        }
        System.out.println(caf.getFingerprint().getHash());
        Fingerprint submitted = fRepo.findFingerprintByHash(caf.getFingerprint().getHash());
    
        if (submitted == null) {
            return false;
        }
    
        Fingerprint expected = fcode.getFingerprint();
        if (expected == null) {
            return false;
        }
        System.out.println(submitted.getHash());
        System.out.println(expected.getHash());
    
        if (!submitted.getHash().equals(expected.getHash())) {
            return false;
        }
    
        expected.setVerified(true);
        fRepo.save(expected);
        return true;
    }
    

    public User getUserbyCode(String code) {
        FACode fcode = cRepo.findFACodeByCode(code);
        if (fcode == null) {
            System.out.println("invalid code");
            return null;
        }
        Fingerprint f = fcode.getFingerprint();
        return f.getUser();
    }

    public Fingerprint createFingerprint(UserAndFingerPrint uaf) {
        return fRepo.save(
                new Fingerprint(uaf.getFingerprint().getHash(), uRepo.findById(uaf.getUser().getId()).orElseThrow()));
    }

}
