package com.chitas.example.model.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetNewPasswordInput {
    @NotBlank
    private String password;
    @NotBlank
    private String code;
    @NotBlank
    private String fingerprint;
}
