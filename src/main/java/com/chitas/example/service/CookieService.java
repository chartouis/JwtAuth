package com.chitas.example.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public void setJwtCookie(String jwtToken, HttpServletResponse response) {
        Cookie cookie = new Cookie("token", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(100);

        // Add SameSite attribute
        String cookieHeader = String.format("%s=%s; Path=%s; HttpOnly; SameSite=strict; Max-Age=%d",
                cookie.getName(),
                cookie.getValue(),
                cookie.getPath(),
                cookie.getMaxAge());
        if (cookie.getSecure()) {
            cookieHeader += "; Secure";
        }
        response.addHeader("Set-Cookie", cookieHeader);
    }
}