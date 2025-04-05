package com.chitas.example.model.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CodeInput {
    @NotBlank
    private String code;
}
