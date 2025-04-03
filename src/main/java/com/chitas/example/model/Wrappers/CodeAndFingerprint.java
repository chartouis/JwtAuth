package com.chitas.example.model.Wrappers;

import com.chitas.example.model.FACode;
import com.chitas.example.model.Fingerprint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeAndFingerprint {
    private FACode code;
    private Fingerprint fingerprint;

}
