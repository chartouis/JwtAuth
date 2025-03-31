package com.chitas.example.service;

import java.io.IOException;

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
public class GoogleAuthFlowService {

    private static final String CLIENT_ID = System
            .getenv("SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID");
    private static final String CLIENT_SECRET = System
            .getenv("SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET");

    public GoogleTokenResponse requestTokens(String authCode) throws IOException {
        try {
            GoogleTokenResponse response = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(), new GsonFactory(),
                    CLIENT_ID, CLIENT_SECRET,
                    authCode, "http://localhost:5173")
                    .execute();
            return response;
        } catch (TokenResponseException e) {
            if (e.getDetails() != null) {
                System.err.println("Error: " + e.getDetails().getError());
                if (e.getDetails().getErrorDescription() != null) {
                    System.err.println(e.getDetails().getErrorDescription());
                }
                if (e.getDetails().getErrorUri() != null) {
                    System.err.println(e.getDetails().getErrorUri());
                }
            } else {
                System.err.println(e.getMessage());
            }
        }
        return null;
    }

    public GoogleCredentials getCredentials(String authCode) throws IOException {
        GoogleTokenResponse gtok = requestTokens(authCode);
        AccessToken tok = new AccessToken(gtok.getAccessToken(), null);
        return GoogleCredentials.create(tok);

    }

    public void printUserInfo(GoogleCredentials credentials) {
        try {

            JsonObject userInfo = getUserJson(credentials);

            System.out.println("User ID: " + userInfo.get("sub").getAsString());
            System.out.println("Email: " + userInfo.get("email").getAsString());
            System.out.println("Name: " + userInfo.get("name").getAsString());
        } catch (Exception e) {
            System.out.println("Error retrieving user info: " + e.getMessage());
        }
    }

    private JsonObject getUserJson(GoogleCredentials credentials) {
        try {
            credentials.refreshIfExpired();
            String accessToken = credentials.getAccessToken().getTokenValue();

            // Set up HTTP request to Userinfo endpoint
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
            GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v3/userinfo");
            HttpRequest request = requestFactory.buildGetRequest(url);
            request.getHeaders().setAuthorization("Bearer " + accessToken);

            // Execute request and get response
            HttpResponse response = request.execute();
            String json = response.parseAsString();

            // Parse JSON response
            return JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            System.out.println("Method getUserInfo returned null");
            return null;
        }
    }

    public UserCreds getUserInfo(GoogleCredentials credentials) {
        JsonObject jsonObject = getUserJson(credentials);
        return new UserCreds(jsonObject.get("sub").getAsString(), jsonObject.get("email").getAsString(),
                jsonObject.get("name").getAsString());
    }

    public String autoEmailToUsername(String email){
        return email.split("@")[0];
    }

}
