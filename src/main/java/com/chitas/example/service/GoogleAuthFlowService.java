package com.chitas.example.service;

import java.io.IOException;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import com.chitas.example.model.UserCreds;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
@Log4j2
public class GoogleAuthFlowService {

    private static final String CLIENT_ID = System
            .getenv("SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID");
    private static final String CLIENT_SECRET = System
            .getenv("SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET");

    public GoogleTokenResponse requestTokens(String authCode) throws IOException {
        try {
            log.info("Requesting tokens for auth code: {}", authCode);
            GoogleTokenResponse response = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(), new GsonFactory(),
                    CLIENT_ID, CLIENT_SECRET,
                    authCode, "http://localhost:5173")
                    .execute();
            log.info("Tokens retrieved successfully");
            return response;
        } catch (TokenResponseException e) {
            log.error("Token request failed: {}", e.getMessage());
            if (e.getDetails() != null) {
                log.error("Error details: {} - {}", e.getDetails().getError(), e.getDetails().getErrorDescription());
            }
        }
        log.warn("Returning null due to token request failure");
        return null;
    }

    public GoogleCredentials getCredentials(String authCode) throws IOException {
        log.info("Getting credentials for auth code: {}", authCode);
        GoogleTokenResponse gtok = requestTokens(authCode);
        AccessToken tok = new AccessToken(gtok.getAccessToken(), null);
        log.info("Credentials created successfully");
        return GoogleCredentials.create(tok);
    }

    public void printUserInfo(GoogleCredentials credentials) {
        try {
            log.info("Printing user info");
            JsonObject userInfo = getUserJson(credentials);
            log.info("User ID: {}, Email: {}, Name: {}", 
                userInfo.get("sub").getAsString(), 
                userInfo.get("email").getAsString(), 
                userInfo.get("name").getAsString());
        } catch (Exception e) {
            log.error("Error retrieving user info: {}", e.getMessage());
        }
    }

    private JsonObject getUserJson(GoogleCredentials credentials) {
        try {
            log.debug("Fetching user JSON");
            credentials.refreshIfExpired();
            String accessToken = credentials.getAccessToken().getTokenValue();
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
            GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v3/userinfo");
            HttpRequest request = requestFactory.buildGetRequest(url);
            request.getHeaders().setAuthorization("Bearer " + accessToken);
            HttpResponse response = request.execute();
            String json = response.parseAsString();
            JsonObject userJson = JsonParser.parseString(json).getAsJsonObject();
            log.debug("User JSON retrieved successfully");
            return userJson;
        } catch (Exception e) {
            log.error("Failed to get user JSON: {}", e.getMessage());
            return null;
        }
    }

    public UserCreds getUserInfo(GoogleCredentials credentials) {
        log.info("Getting user info from credentials");
        JsonObject jsonObject = getUserJson(credentials);
        UserCreds userCreds = new UserCreds(jsonObject.get("sub").getAsString(), 
                jsonObject.get("email").getAsString(), 
                jsonObject.get("name").getAsString());
        log.info("User info retrieved: {}", userCreds.getEmail());
        return userCreds;
    }

    public String autoEmailToUsername(String email){
        String username = email.split("@")[0];
        log.debug("Converted email {} to username: {}", email, username);
        return username;
    }
}