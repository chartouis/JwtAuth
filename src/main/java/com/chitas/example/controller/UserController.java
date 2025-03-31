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
import com.chitas.example.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping
public class UserController {

    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserDTO register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public JWT login(@RequestBody User user, HttpServletResponse response){
        return userService.verify(user,response);
    }

    @GetMapping("/refresh")
    public JWT refresh(HttpServletResponse response) {
        return userService.refresh(response);
    }

    @PostMapping("/oauth")
    public String googleOauth(@RequestBody AuthCode code, HttpServletResponse response) {
        return userService.googleOauth(code, response);
    }
    
    
    

    @GetMapping("/test") //This is useless. You can freely delete it
    public String postMethodName() {
        return "SUCCESS";
    }


}