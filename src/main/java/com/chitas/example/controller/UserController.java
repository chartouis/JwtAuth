package com.chitas.example.controller;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chitas.example.model.JWT;
import com.chitas.example.model.User;
import com.chitas.example.model.DTO.UserDTO;
import com.chitas.example.repo.UsersRepo;
import com.chitas.example.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping
public class UserController {

    private final UserService userService;


    
    public UserController(UsersRepo usersRepo, UserService userService) {
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
    

    @PostMapping("/test") //This is useless. You can freely delete it
    public JWT postMethodName(@RequestBody JWT entity) {
        return entity;
    }
    


}