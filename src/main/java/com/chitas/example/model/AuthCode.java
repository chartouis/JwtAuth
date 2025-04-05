package com.chitas.example.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthCode {
    @NotBlank
    private String code;


}