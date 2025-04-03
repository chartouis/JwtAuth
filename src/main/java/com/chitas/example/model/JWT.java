package com.chitas.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JWT {
    private String token;

    public JWT(String token){
        this.token = token;
    }
}
