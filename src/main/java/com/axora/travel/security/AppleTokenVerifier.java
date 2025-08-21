// ▶ AppleTokenVerifier start
package com.axora.travel.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObjectType;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;

@Component
public class AppleTokenVerifier {

  private final String expectedIssuer;
  private final String expectedAudience;

  private final RemoteJWKSet<SecurityContext> jwkSource;

  public AppleTokenVerifier(
      @Value("${security.apple.issuer}") String issuer,
      @Value("${security.apple.audience}") String audience
  ) throws Exception {
    this.expectedIssuer = issuer;
    this.expectedAudience = audience;
    var retriever = new DefaultResourceRetriever(5000, 5000);
    this.jwkSource = new RemoteJWKSet<>(new URL("https://appleid.apple.com/auth/keys"), retriever);
  }

  public JWTClaimsSet verify(String idToken) throws Exception {
    SignedJWT jwt = SignedJWT.parse(idToken);
    JWSHeader header = jwt.getHeader();

    if (header.getType() != null && !JWSObjectType.JWT.equals(header.getType())) {
      throw new IllegalArgumentException("Unexpected header typ: " + header.getType());
    }
    if (!JWSAlgorithm.RS256.equals(header.getAlgorithm())) {
      throw new IllegalArgumentException("Apple token must be RS256");
    }

    var selector = new com.nimbusds.jose.proc.JWSKeySelector<SecurityContext>() {
      @Override
      public List<? extends JWK> selectJWSKeys(JWSHeader jwsHeader, SecurityContext context) {
        try {
          return jwkSource.get(jwsHeader, context);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
    var keys = selector.selectJWSKeys(header, null);
    if (keys.isEmpty()) throw new IllegalArgumentException("Apple key not found for kid=" + header.getKeyID());

    JWK jwk = keys.get(0);
    RSAKey rsa = jwk.toRSAKey();
    JWSVerifier verifier = new RSASSAVerifier(rsa);

    if (!jwt.verify(verifier)) {
      throw new IllegalArgumentException("Invalid Apple signature");
    }

    JWTClaimsSet claims = jwt.getJWTClaimsSet();
    if (!expectedIssuer.equals(claims.getIssuer())) {
      throw new IllegalArgumentException("Invalid iss");
    }
    if (claims.getAudience() == null || claims.getAudience().isEmpty() ||
        !claims.getAudience().contains(expectedAudience)) {
      throw new IllegalArgumentException("Invalid aud");
    }
    return claims;
  }
}
// ◀ AppleTokenVerifier end
