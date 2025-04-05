package com.chitas.example.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chitas.example.model.AuthCode;
import com.chitas.example.model.JWT;
import com.chitas.example.model.DTO.UserDTO;
import com.chitas.example.model.Wrappers.CodeAndFingerprint;
import com.chitas.example.model.Wrappers.UserAndFingerPrint;
import com.chitas.example.service.UserService;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;

    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserAndFingerPrint wrapper) {

        UserDTO registeredUser = userService.register(wrapper.getUser());
        if (!userService.isVerifiedUser(wrapper)) {
            return ResponseEntity.status(HttpStatus.CONTINUE).body(new UserDTO(0L, "2FA",
                    "The code was sent to this email" + wrapper.getUser().getEmail(), LocalDateTime.now()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<JWT> login(@RequestBody UserAndFingerPrint wrapper, HttpServletResponse response) {

        JWT token = userService.verify(wrapper.getUser(), response);
        if (!userService.isVerifiedUser(wrapper)) {
            return ResponseEntity.status(HttpStatus.CONTINUE).body(new JWT("VERIFY EMAIL"));
        }
        return ResponseEntity.ok(token);
    }

    @GetMapping("/refresh")
    public ResponseEntity<JWT> refresh(HttpServletResponse response) {
        JWT token = userService.refresh(response);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/oauth")
    public ResponseEntity<String> googleOauth(@RequestBody AuthCode code, HttpServletResponse response) {
        String result = userService.googleOauth(code, response);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/code")
    public ResponseEntity<String> validateUser(@RequestBody CodeAndFingerprint caf) {
        String result = userService.validateUser(caf);
        return ResponseEntity.ok(result);
    }

    // This is useless. You can freely delete it
    @GetMapping("/api/test")
    public String postMethodName() {
        return "SUCCESS";
    }

}