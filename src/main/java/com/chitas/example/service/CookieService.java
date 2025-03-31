package com.chitas.example.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public String setCookie(String jwtToken, HttpServletResponse response, String name, String path, int age) {

        ResponseCookie cookie = ResponseCookie.from(name, jwtToken) 
                .path(path)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .maxAge(age)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return cookie.toString();
    }

    public String getToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
    
        for (Cookie ck : cookies) {
            String name = ck.getName();
            if ("REFRESH-TOKEN-JWTAUTH".equals(name) || "ACCESS-TOKEN-JWTAUTH".equals(name)) {
                return ck.getValue();
            }
        }
        return null;
    }
}