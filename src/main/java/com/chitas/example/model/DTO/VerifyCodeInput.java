package com.chitas.example.model.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyCodeInput {
    @NotBlank
    private String code;
    @NotBlank
    private String fingerprint;
}
