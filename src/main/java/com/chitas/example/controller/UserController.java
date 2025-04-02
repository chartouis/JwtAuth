package com.chitas.example.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chitas.example.model.AuthCode;
import com.chitas.example.model.JWT;
import com.chitas.example.model.User;
import com.chitas.example.model.DTO.UserDTO;
import com.chitas.example.service.MailService;
import com.chitas.example.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping
public class UserController {

    private final UserService userService;
    private final MailService mailService; // test

    public UserController(UserService userService, MailService mailService) {
        this.userService = userService;
        this.mailService = mailService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody User user) {
        UserDTO registeredUser = userService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<JWT> login(@RequestBody User user, HttpServletResponse response) {
        JWT token = userService.verify(user, response);
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

    // This is useless. You can freely delete it
    @GetMapping("/api/test")
    public String postMethodName() {
        return "SUCCESS";
    }

    @GetMapping("/api/mail")
    public void mailSomething() {

        mailService.sendSimpleMessage("erna95er@gmail.com", "я научился спамить",
                "ну все пиздец. я по идее могу 500 вот таких мейлов высылать в день");

    }

}