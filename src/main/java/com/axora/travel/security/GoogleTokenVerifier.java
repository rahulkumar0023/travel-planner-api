// --- GoogleTokenVerifier start ---
package com.axora.travel.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleTokenVerifier {

  private final GoogleIdTokenVerifier verifier;

  public record Profile(String userId, String email) {}

  public GoogleTokenVerifier(@Value("${security.google.clientId:YOUR_WEB_OAUTH_CLIENT_ID}") String clientId) throws Exception {
    this.verifier = new GoogleIdTokenVerifier.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),
        GsonFactory.getDefaultInstance()
    )
    .setAudience(Collections.singletonList(clientId))
    .build();
  }

  public Profile verify(String idTokenString) throws Exception {
    GoogleIdToken idToken = verifier.verify(idTokenString);
    if (idToken == null) {
      throw new IllegalArgumentException("Invalid Google ID token");
    }
    final var payload = idToken.getPayload();
    final String userId = payload.getSubject();
    final String email = (String) payload.get("email");
    return new Profile(userId, email);
  }
}
// --- GoogleTokenVerifier end ---
