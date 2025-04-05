package com.chitas.example.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.chitas.example.model.AuthCode;
import com.chitas.example.model.Fingerprint;
import com.chitas.example.model.JWT;
import com.chitas.example.model.User;
import com.chitas.example.model.UserCreds;
import com.chitas.example.model.DTO.UserDTO;
import com.chitas.example.repo.UsersRepo;
import com.chitas.example.utils.RandomStringUtil;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Log4j2
public class UserService {
    private final UsersRepo repo;
    private final GoogleAuthFlowService gFlowService;
    private final AuthenticationManager manager;
    private final JWTService jwtService;
    private final CookieService cook;
    private final int REFRESH_TOKEN_AGE = 60 * 60 * 24 * 30;
    private final int ACCESS_TOKEN_AGE = 60 * 10;
    private final TwoFactorService twoFactorService;
    private final MailService mailService;

    public UserService(UsersRepo repo, AuthenticationManager manager, JWTService jwtservice, CookieService cook,
            GoogleAuthFlowService gFlowService, TwoFactorService twoFactorService, MailService mailService) {
        this.repo = repo;
        this.gFlowService = gFlowService;
        this.manager = manager;
        this.jwtService = jwtservice;
        this.cook = cook;
        this.twoFactorService = twoFactorService;
        this.mailService = mailService;
    }

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserDTO register(User user) {
        if (user.getPassword() == null || !isValidEmail(user.getEmail())) {
            log.warn("Invalid registration attempt: null password or invalid email {}", user.getEmail());
            return null;
        }
        if (user.getUsername() == null) {
            user.setUsername(user.getEmail().split("@")[0]);
        }
        if (repo.existsByUsername(user.getUsername())) {
            log.warn("Username {} already exists", user.getUsername());
            return getDefaultUserDTO();
        }
        if (repo.existsByEmail(user.getEmail())) {
            log.warn("Email {} already exists", user.getEmail());
            return getDefaultUserDTO();
        }
        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);
        log.info("User registered: {}", user.getUsername());
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }

    public JWT verify(User user, HttpServletResponse response) {
        log.info("Verifying user: {}", user.getUsername());
        Authentication authentication = manager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        if (authentication.isAuthenticated()) {
            String tok = jwtService.generateToken(user.getUsername());
            cook.setCookie(tok, response, "REFRESH-TOKEN-JWTAUTH", "/refresh", REFRESH_TOKEN_AGE);
            log.info("User {} authenticated successfully", user.getUsername());
            return new JWT("SUCCESS");
        }
        log.warn("Authentication failed for user: {}", user.getUsername());
        return new JWT("FAILURE");
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\\.[a-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public UserDTO userToDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }

    private UserDTO getDefaultUserDTO() {
        String cause = "Invalid username or password.";
        return new UserDTO(0L, "Failed to register", cause, LocalDateTime.MIN);
    }

    public JWT refresh(HttpServletResponse response) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Refreshing token for user: {}", username);
        String tok = jwtService.generateToken(username);
        if (tok == null) {
            log.warn("Token generation failed for user: {}", username);
            return new JWT("FAILURE");
        }
        cook.setCookie(tok, response, "ACCESS-TOKEN-JWTAUTH", "/api", ACCESS_TOKEN_AGE);
        log.info("Token refreshed for user: {}", username);
        return new JWT("SUCCESS");
    }

    public String googleOauth(AuthCode code, HttpServletResponse response) {
        try {
            log.info("Starting Google OAuth for code: {}", code.getCode());
            GoogleCredentials googleCredentials = gFlowService.getCredentials(code.getCode());
            UserCreds userCreds = gFlowService.getUserInfo(googleCredentials);
            User user;
            if (repo.existsByEmail(userCreds.getEmail())) {
                user = repo.findByEmail(userCreds.getEmail());
                log.info("Existing user found: {}", user.getUsername());
            } else {
                User newUser = new User();
                newUser.setEmail(userCreds.getEmail());
                newUser.setUsername(gFlowService.autoEmailToUsername(userCreds.getEmail()));
                newUser.setPassword(encoder.encode(RandomStringUtil.generate(40)));
                repo.save(newUser);
                user = newUser;
                log.info("New user created via Google OAuth: {}", user.getUsername());
            }
            String tok = jwtService.generateToken(user.getUsername());
            cook.setCookie(tok, response, "REFRESH-TOKEN-JWTAUTH", "/refresh", REFRESH_TOKEN_AGE);
            log.info("Google OAuth successful for user: {}", user.getUsername());
            return "SUCCESS";
        } catch (IOException e) {
            log.error("Google OAuth failed: {}", e.getMessage());
            return "FAILURE";
        }
    }

    public String validateUser(String code, String hash) {
        log.info("Validating user with code: {} and hash: {}", code, hash);
        boolean result = twoFactorService.verifyFingerprint(code, hash);
        if (result) {
            User user = twoFactorService.getUserbyCode(code);
            register(user);
            log.info("User validated and registered: {}", user.getUsername());
            return "SUCCCESS";
        } else {
            log.warn("Validation failed for code: {} and hash: {}", code, hash);
            return "FAILURE";
        }
    }

    public boolean isVerifiedUser(String emailOrUsername, String hash, boolean isLogin) {
        String email = emailOrUsername;
        log.info("Checking verification for: {} with hash: {}", emailOrUsername, hash);
        if (isLogin && repo.existsByUsername(emailOrUsername)) {
            email = repo.findByUsername(emailOrUsername).getEmail();
        }
        if (twoFactorService.getFingerprintByHash(hash) != null) {
            if (twoFactorService.getFingerprintByHash(hash).isVerified()) {
                log.info("User {} is verified", email);
                return true;
            }
        }
        if (!twoFactorService.fingerprintExists(hash)) {
            if (!repo.existsByEmail(email)) {
                log.warn("No user found for email: {}", email);
                return false;
            }
            User user = repo.findByEmail(email);
            Fingerprint f = twoFactorService.createFingerprint(hash, user);
            mailService.sendVerficationCode(f);
            log.info("Verification code sent to: {}", email);
            return false;
        }
        Fingerprint f = twoFactorService.getFingerprintByHash(hash);
        mailService.sendVerficationCode(f);
        log.info("Verification code resent to: {}", email);
        return false;
    }
}