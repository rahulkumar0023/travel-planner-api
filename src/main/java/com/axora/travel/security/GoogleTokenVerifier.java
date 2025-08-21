// --- GoogleTokenVerifier start ---
package com.axora.travel.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class GoogleTokenVerifier {
  private final GoogleIdTokenVerifier verifier;

  public GoogleTokenVerifier(@Value("${security.google.audiences}") String audiencesCsv) {
    List<String> audiences = Arrays.stream(audiencesCsv.split(","))
        .map(String::trim).filter(s -> !s.isBlank()).toList();

    this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
        .setAudience(audiences)
        .build();
  }

  public GoogleIdToken.Payload verify(String idToken) throws Exception {
    GoogleIdToken token = verifier.verify(idToken);
    if (token == null) throw new IllegalArgumentException("Invalid Google ID token");
    var p = token.getPayload();
    if (p.getEmail() == null) throw new IllegalArgumentException("Google token missing email");
    if (!Boolean.TRUE.equals(p.getEmailVerified())) {
      throw new IllegalArgumentException("Google email not verified");
    }
    return p;
  }
}
// --- GoogleTokenVerifier end ---
