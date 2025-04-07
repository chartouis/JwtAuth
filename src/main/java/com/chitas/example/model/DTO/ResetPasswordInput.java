package com.chitas.example.model.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordInput {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String fingerprint;
}
