package com.axora.travel.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;

@Service
public class JwtService {
  private final Algorithm algo;
  private final String issuer;
  private final long ttlSeconds;
  private final JWTVerifier verifier;
//  private final Clock clock;

  public JwtService(
          @Value("${security.jwt.secret}") String secret,
          @Value("${security.jwt.issuer}") String issuer,
          @Value("${security.jwt.ttlMinutes}") long ttlMinutes
        //  Clock clock // provide a @Bean Clock.systemUTC()
  ) {
    if (secret == null || secret.isBlank()) throw new IllegalArgumentException("JWT secret missing");
    if (ttlMinutes <= 0) throw new IllegalArgumentException("JWT TTL must be > 0");
    this.algo = Algorithm.HMAC256(secret.getBytes(StandardCharsets.UTF_8));
    this.issuer = issuer;
    this.ttlSeconds = ttlMinutes * 60;
  //  this.clock = clock;

    // add leeway for minor clock drift
    this.verifier = JWT.require(algo).withIssuer(issuer).acceptLeeway(60).build();
  }

  public String issue(String userId, String email, Set<String> roles) {
    Instant now = Instant.now();
    String[] roleArray = (roles == null) ? new String[]{} : roles.toArray(String[]::new);

    return JWT.create()
            .withIssuer(issuer)
            .withSubject(userId)
            // .withAudience("axora-travel-api") // optional: lock to intended audience
            .withClaim("email", email)
            .withArrayClaim("roles", roleArray)
            .withIssuedAt(now)
            .withExpiresAt(now.plusSeconds(ttlSeconds))
            // .withKeyId("k1") // optional: for key rotation
            .sign(algo);
  }

  public DecodedJWT verify(String token) {
    try {
      return verifier.verify(token);
    } catch (JWTVerificationException e) {
      // wrap to your domain exception if you prefer
      throw e;
    }
  }

  // convenience accessors
  public String getUserId(DecodedJWT jwt) { return jwt.getSubject(); }
  public String getEmail(DecodedJWT jwt) { return jwt.getClaim("email").asString(); }
  public Set<String> getRoles(DecodedJWT jwt) {
    String[] arr = jwt.getClaim("roles").asArray(String.class);
    return arr == null ? Set.of() : Set.of(arr);
  }
}
