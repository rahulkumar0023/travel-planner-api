// --- AppleTokenVerifier start ---
package com.axora.travel.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.ParseException;
import java.util.List;

@Component
public class AppleTokenVerifier {
  private final String issuer;
  private final String audience;
  private final DefaultResourceRetriever retriever = new DefaultResourceRetriever(5000, 5000);

  public AppleTokenVerifier(
      @Value("${security.apple.issuer}") String issuer,
      @Value("${security.apple.audience}") String audience) {
    this.issuer = issuer;
    this.audience = audience;
  }

  public com.nimbusds.jwt.JWTClaimsSet verify(String idToken) throws Exception {
    SignedJWT jwt = SignedJWT.parse(idToken);
    JWSHeader header = jwt.getHeader();

    // Fetch JWKS
    var jwks = JWKSet.load(new URL("https://appleid.apple.com/auth/keys"), retriever);
    List<JWK> matches = jwks.getKeys().stream()
        .filter(j -> j.getKeyID().equals(header.getKeyID()))
        .toList();
    if (matches.isEmpty()) throw new IllegalArgumentException("Apple JWK not found");

    JWK jwk = matches.get(0);
    if (!(jwk instanceof ECKey ecKey)) throw new IllegalArgumentException("Apple key not EC");
    JWSVerifier verifier = new ECDSAVerifier(ecKey.toECPublicKey());
    if (!jwt.verify(verifier)) throw new IllegalArgumentException("Invalid Apple signature");

    var claims = jwt.getJWTClaimsSet();
    if (!issuer.equals(claims.getIssuer())) throw new IllegalArgumentException("Bad issuer");
    if (!audience.equals(claims.getAudience().get(0))) throw new IllegalArgumentException("Bad audience");
    return claims;
  }
}
// --- AppleTokenVerifier end ---
