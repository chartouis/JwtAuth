package com.chitas.example.service;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.chitas.example.model.JWT;
import com.chitas.example.model.User;
import com.chitas.example.model.DTO.UserDTO;
import com.chitas.example.repo.UsersRepo;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService {
    private final UsersRepo repo;
    private final AuthenticationManager manager;
    private final JWTService jwtService;
    CookieService cook;

    public UserService(UsersRepo repo, AuthenticationManager manager, JWTService jwtservice, CookieService cook) {
        this.repo = repo;
        this.manager = manager;
        this.jwtService = jwtservice;
        this.cook = cook;
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
            cook.setJwtCookie(tok, response);
            return new JWT(tok);
        }
        return new JWT("failure");
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
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

}
