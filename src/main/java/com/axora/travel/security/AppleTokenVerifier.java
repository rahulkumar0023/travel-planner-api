// ▶︎ AppleTokenVerifier.java — REPLACE FILE START
package com.axora.travel.security;

// Description: Verifies Apple identity token (JWT) using RS256 and Apple JWKS.
// No JWSObjectType, no custom KeySelector — just match kid and verify.

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;

@Component
public class AppleTokenVerifier {

  private final String expectedIssuer;
  private final String expectedAudience;

  public AppleTokenVerifier(
          @Value("${security.apple.issuer}") String issuer,
          @Value("${security.apple.audience}") String audience) {
    this.expectedIssuer = issuer;     // usually "https://appleid.apple.com"
    this.expectedAudience = audience; // your Services ID or bundle ID
  }

  public JWTClaimsSet verify(String idToken) throws Exception {
    // Parse token & header
    SignedJWT jwt = SignedJWT.parse(idToken);
    JWSHeader header = jwt.getHeader();

    if (!JWSAlgorithm.RS256.equals(header.getAlgorithm())) {
      throw new IllegalArgumentException("Apple token must use RS256");
    }

    // Fetch Apple JWKS and select the JWK by 'kid'
    JWKSet jwks = JWKSet.load(new URL("https://appleid.apple.com/auth/keys"));
    List<JWK> matches = new JWKSelector(new JWKMatcher.Builder()
            .keyID(header.getKeyID()).build()).select(jwks);

    if (matches.isEmpty()) {
      throw new IllegalArgumentException("Apple public key not found for kid=" + header.getKeyID());
    }

    RSAKey rsa = matches.get(0).toRSAKey();
    JWSVerifier verifier = new RSASSAVerifier(rsa);

    // Verify signature
    if (!jwt.verify(verifier)) {
      throw new IllegalArgumentException("Invalid Apple signature");
    }

    // Validate claims
    JWTClaimsSet claims = jwt.getJWTClaimsSet();
    if (!expectedIssuer.equals(claims.getIssuer())) {
      throw new IllegalArgumentException("Invalid iss: " + claims.getIssuer());
    }
    if (claims.getAudience() == null || claims.getAudience().isEmpty()
            || !claims.getAudience().contains(expectedAudience)) {
      throw new IllegalArgumentException("Invalid aud: " + claims.getAudience());
    }
    return claims;
  }
}
// ◀︎ AppleTokenVerifier.java — REPLACE FILE END
