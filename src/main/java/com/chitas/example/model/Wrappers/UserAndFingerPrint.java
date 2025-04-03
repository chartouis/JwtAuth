package com.chitas.example.model.Wrappers;

import com.chitas.example.model.Fingerprint;
import com.chitas.example.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAndFingerPrint {
    private User user;
    private Fingerprint fingerprint;

}
