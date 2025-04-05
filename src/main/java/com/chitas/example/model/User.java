package com.chitas.example.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.chitas.example.model.DTO.LoginInput;
import com.chitas.example.model.DTO.RegisterInput;

import java.time.LocalDateTime;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @CreatedDate
    private LocalDateTime createdAt;

    public User(RegisterInput reg){
        this.email = reg.getEmail();
        this.username = reg.getUsername();
        this.password = reg.getPassword();
    }

    public User(LoginInput login){
        this.username = login.getUsername();
        this.password = login.getPassword();
    }

}
