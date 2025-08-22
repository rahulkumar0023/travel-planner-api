// --- AppleTokenVerifier start ---
package com.axora.travel.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AppleTokenVerifier {

  private static final String APPLE_ISS = "https://appleid.apple.com";
  private final Set<String> audience;
  private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

  public record Profile(String userId, String email) {}

  public AppleTokenVerifier(@Value("${security.apple.audience:com.your.bundle.id}") String audienceCsv) throws Exception {
    this.audience = Arrays.stream(audienceCsv.split(","))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .collect(Collectors.toSet());
    var jwkSetURL = new URL("https://appleid.apple.com/auth/keys");
    var keySource = new RemoteJWKSet<SecurityContext>(jwkSetURL);
    this.jwtProcessor = new DefaultJWTProcessor<>();
    this.jwtProcessor.setJWSKeySelector(
        new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource));
  }

  public Profile verify(String identityToken) throws Exception {
    try {
      JWTClaimsSet claims = jwtProcessor.process(identityToken, null);
      if (!APPLE_ISS.equals(claims.getIssuer())) {
        throw new IllegalArgumentException("Invalid Apple iss");
      }
      if (claims.getAudience() == null ||
          claims.getAudience().stream().noneMatch(audience::contains)) {
        throw new IllegalArgumentException("Invalid Apple aud");
      }
      final String userId = claims.getSubject();
      final String email = (String) claims.getClaim("email");
      return new Profile(userId, email != null ? email : (userId + "@apple.local"));
    } catch (BadJOSEException e) {
      throw new IllegalArgumentException("Invalid Apple token: " + e.getMessage(), e);
    }
  }
}
// --- AppleTokenVerifier end ---
