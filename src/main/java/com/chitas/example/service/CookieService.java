package com.chitas.example.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public void setCookie(String jwtToken, HttpServletResponse response, String path) {
        Cookie cookie = new Cookie("token", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath(path);
        cookie.setMaxAge(100); //change on deploy

        String cookieHeader = String.format("%s=%s; Path=%s; HttpOnly; SameSite=Strict; Max-Age=%d",
                cookie.getName(),
                cookie.getValue(),
                cookie.getPath(),
                cookie.getMaxAge());
        if (cookie.getSecure()) {
            cookieHeader += "; Secure";
        }
        response.addHeader("Set-Cookie", cookieHeader);
    }

    public String getToken(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if (cookies == null){return null;}
        Cookie cookie = cookies[0];
        String token = cookie.getValue();
        System.out.println(token);
        return token;
    }
}