package com.chitas.example.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public String setCookie(String jwtToken, HttpServletResponse response, String name ,String path, int age) {
        Cookie cookie = new Cookie(name, jwtToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // SET TO TRUE IN PRODUCTION
        cookie.setPath(path);
        cookie.setMaxAge(age);

        String cookieHeader = String.format("%s=%s; Path=%s; HttpOnly; SameSite=Strict; Max-Age=%d",
                cookie.getName(),
                cookie.getValue(),
                cookie.getPath(),
                cookie.getMaxAge());
        if (cookie.getSecure()) {
            cookieHeader += "; Secure";
        }
        response.addHeader("Set-Cookie", cookieHeader);
        return cookieHeader;
    }

    public String getToken(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if (cookies == null){return null;}
        Cookie cookie = cookies[0];
        String token = cookie.getValue();
        return token;
    }
}