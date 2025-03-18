package com.chitas.example.model;

import lombok.Data;

@Data
public class JWT {
    private String token;

    public JWT(String token){
        this.token = token;
    }
}
