package com.chitas.example.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chitas.example.model.AuthCode;
import com.chitas.example.model.JWT;
import com.chitas.example.model.User;
import com.chitas.example.model.DTO.LoginInput;
import com.chitas.example.model.DTO.RegisterInput;
import com.chitas.example.model.DTO.ResetPasswordInput;
import com.chitas.example.model.DTO.SetNewPasswordInput;
import com.chitas.example.model.DTO.UserDTO;
import com.chitas.example.model.DTO.VerifyCodeInput;
import com.chitas.example.service.UserService;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping
@Log4j2
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody @Valid RegisterInput reg) {
        log.info("Register request for email: {}", reg.getEmail());
        UserDTO registeredUser = userService.register(new User(reg));
        if (!userService.isVerifiedUser(reg.getEmail(), reg.getFingerprint(), false)) {
            log.info("2FA required for email: {}", reg.getEmail());
            return ResponseEntity.status(HttpStatus.CONTINUE).body(new UserDTO(0L, "2FA",
                    "The code was sent to this email" + reg.getEmail(), LocalDateTime.now()));
        }
        log.info("User registered successfully: {}", reg.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<JWT> login(@RequestBody @Valid LoginInput login, HttpServletResponse response) {
        log.info("Login request for username: {}", login.getUsername());
        JWT token = userService.verify(new User(login), response);
        if (!userService.isVerifiedUser(login.getUsername(), login.getFingerprint(), true)) {
            log.info("Email verification required for: {}", login.getUsername());
            return ResponseEntity.status(HttpStatus.CONTINUE).body(new JWT("VERIFY EMAIL"));
        }
        log.info("Login successful for: {}", login.getUsername());
        return ResponseEntity.ok(token);
    }

    @GetMapping("/refresh")
    public ResponseEntity<JWT> refresh(HttpServletResponse response) {
        log.info("Token refresh request");
        JWT token = userService.refresh(response);
        log.info("Token refreshed successfully");
        return ResponseEntity.ok(token);
    }

    @PostMapping("/oauth")
    public ResponseEntity<String> googleOauth(@RequestBody @Valid AuthCode code, HttpServletResponse response) {
        log.info("Google OAuth request with code: {}", code.getCode());
        String result = userService.googleOauth(code, response);
        log.info("Google OAuth result: {}", result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/code")
    public ResponseEntity<String> validateUser(@RequestBody @Valid VerifyCodeInput input) {
        log.info("Validating user with code: {} and fingerprint: {}", input.getCode(), input.getFingerprint());
        String result = userService.validateUser(input.getCode(), input.getFingerprint());
        log.info("Validation result: {}", result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordInput reset) {
        log.info("Resetting password with email: {} and fingerprint: {}", reset.getEmail(), reset.getFingerprint());

        userService.isVerifiedUser(reset.getEmail(), reset.getFingerprint() + "_PASSWORD_RESET", false);
        log.info("Password reset verification sent to : {}", reset.getEmail());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("VERIFY EMAIL");

    }

    @PostMapping("/newpassword")
    public ResponseEntity<String> setNewPassword(@RequestBody @Valid SetNewPasswordInput newPasswordInput) {
        log.info("Validating user with code: {} and fingerprint: {}", newPasswordInput.getCode(),
                newPasswordInput.getFingerprint());
        String result = userService.validateUser(newPasswordInput.getCode(), newPasswordInput.getFingerprint()+"_PASSWORD_RESET");
        log.info("Validation result: {}", result);
        if(result.equals("SUCCESS")){
            userService.setNewPassword(newPasswordInput.getPassword(),newPasswordInput.getFingerprint()+"_PASSWORD_RESET", newPasswordInput.getCode());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/test")
    public String postMethodName() {
        log.info("Test endpoint called");
        return "SUCCESS";
    }
}