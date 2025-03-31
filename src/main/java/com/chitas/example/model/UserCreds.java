package com.chitas.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCreds {
    
    private String UsedId;
    private String Email;
    private String Name;

}
