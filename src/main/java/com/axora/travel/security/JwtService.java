// --- JwtService start ---
package com.axora.travel.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
public class JwtService {
  private final Algorithm algo;
  private final String issuer;
  private final long ttlSeconds;

  public JwtService(
      @Value("${security.jwt.secret}") String secret,
      @Value("${security.jwt.issuer}") String issuer,
      @Value("${security.jwt.ttlMinutes}") long ttlMinutes) {
    this.algo = Algorithm.HMAC256(secret);
    this.issuer = issuer;
    this.ttlSeconds = ttlMinutes * 60;
  }

  // issue app JWT
  public String issue(String userId, String email, Set<String> roles) {
    return JWT.create()
        .withIssuer(issuer)
        .withSubject(userId)
        .withClaim("email", email)
        .withArrayClaim("roles", roles == null ? new String[]{} : roles.toArray(String[]::new))
        .withIssuedAt(Instant.now())
        .withExpiresAt(Instant.now().plusSeconds(ttlSeconds))
        .sign(algo);
  }

  // verify app JWT
  public DecodedJWT verify(String token) {
    return JWT.require(algo).withIssuer(issuer).build().verify(token);
  }
}
// --- JwtService end ---
