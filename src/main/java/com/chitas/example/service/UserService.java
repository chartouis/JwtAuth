package com.chitas.example.service;

import jakarta.servlet.http.HttpServletResponse;

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
public class UserService {
    private final UsersRepo repo;
    private final GoogleAuthFlowService gFlowService;
    private final AuthenticationManager manager;
    private final JWTService jwtService;
    private final CookieService cook;
    private final int REFRESH_TOKEN_AGE = 60 * 60 * 24 * 30; // Basically a Month
    private final int ACCESS_TOKEN_AGE = 60 * 10;
    private final TwoFactorService twoFactorService;
    private final MailService mailService; // Basically 10 minutes. Consider changing to a negative value

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
            return null;
        }
        if (user.getUsername() == null) {
            user.setUsername(user.getEmail().split("@")[0]);
        }
        if (repo.existsByUsername(user.getUsername())) {
            return getDefaultUserDTO();
        }
        if (repo.existsByEmail(user.getEmail())) {
            return getDefaultUserDTO();
        }
        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }

    public JWT verify(User user, HttpServletResponse response) {
        Authentication authentication = manager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        if (authentication.isAuthenticated()) {
            String tok = jwtService.generateToken(user.getUsername());
            cook.setCookie(tok, response, "REFRESH-TOKEN-JWTAUTH", "/refresh", REFRESH_TOKEN_AGE);
            return new JWT("SUCCESS");
        }
        return new JWT("FAILURE");
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,}$";

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
        String tok = jwtService.generateToken(SecurityContextHolder.getContext().getAuthentication().getName());
        if (tok == null) {
            return new JWT("FAILURE");
        }
        cook.setCookie(tok, response, "ACCESS-TOKEN-JWTAUTH", "/api", ACCESS_TOKEN_AGE);
        return new JWT("SUCCESS");
    }

    public String googleOauth(AuthCode code, HttpServletResponse response) {
        try {
            GoogleCredentials googleCredentials = gFlowService.getCredentials(code.getCode());
            UserCreds userCreds = gFlowService.getUserInfo(googleCredentials);
            User user;
            if (repo.existsByEmail(userCreds.getEmail())) {
                user = repo.findByEmail(userCreds.getEmail());

            } else {
                User newUser = new User();
                newUser.setEmail(userCreds.getEmail());
                newUser.setUsername(gFlowService.autoEmailToUsername(userCreds.getEmail()));
                newUser.setPassword(encoder.encode(RandomStringUtil.generate(40)));
                repo.save(newUser);
                user = newUser;

            }
            String tok = jwtService.generateToken(user.getUsername());
            cook.setCookie(tok, response, "REFRESH-TOKEN-JWTAUTH", "/refresh", REFRESH_TOKEN_AGE);
            return "SUCCESS";

        } catch (IOException e) {
            return "FAILURE";
        }

    }

    public String validateUser(String code, String hash) {

        boolean result = twoFactorService.verifyFingerprint(code, hash);
        if (result) {
            User user = twoFactorService.getUserbyCode(code);
            register(user);
            return "SUCCCESS";
        } else {
            return "FAILURE";
        }

    }

    public boolean isVerifiedUser(String emailOrUsername, String hash, boolean isLogin) {
        String email = emailOrUsername;
        if (isLogin && repo.existsByUsername(emailOrUsername)) {
            email = repo.findByUsername(emailOrUsername).getEmail();
        }
        if (twoFactorService.getFingerprintByHash(hash) != null) {
            if (twoFactorService.getFingerprintByHash(hash).isVerified()) {
                return true;
            }
        }
        if (!twoFactorService.fingerprintExists(hash)) {
            if (!repo.existsByEmail(email)) {
                return false;
            }
            User user = repo.findByEmail(email);
            Fingerprint f = twoFactorService.createFingerprint(hash, user);
            mailService.sendVerficationCode(f);
            return false;
        }

        Fingerprint f = twoFactorService.getFingerprintByHash(hash);
        mailService.sendVerficationCode(f);

        return false;
    }
}
