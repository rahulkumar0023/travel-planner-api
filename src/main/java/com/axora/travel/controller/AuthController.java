// --- AuthController start ---
package com.axora.travel.controller;

import com.axora.travel.security.AppPrincipal;
import com.axora.travel.security.GoogleTokenVerifier;
import com.axora.travel.security.AppleTokenVerifier;
import com.axora.travel.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins="*")
@Slf4j
public class AuthController {

  private final GoogleTokenVerifier google;
  private final AppleTokenVerifier apple;
  private final JwtService jwt;

  public AuthController(GoogleTokenVerifier google, AppleTokenVerifier apple, JwtService jwt) {
    this.google = google;
    this.apple = apple;
    this.jwt = jwt;
  }

  public record IdTokenReq(String idToken) {}
  public record TokenRes(String jwt) {}

  // 👇 NEW: /auth/dev — issue JWT for manual testing (gated by security.devAuthEnabled)
  // dev auth endpoint start
  @Value("${security.devAuthEnabled:false}")
  private boolean devAuthEnabled;

  public record DevReq(String email, String userId) {}

  @PostMapping("/dev")
  public ResponseEntity<?> dev(@RequestBody DevReq req) {
    if (!devAuthEnabled) {
      return ResponseEntity.status(403).body(Map.of("error", "dev_auth_disabled"));
    }

    String email = (req.email() == null || req.email().isBlank())
            ? "tester@example.com"
            : req.email().trim();

    String userId = (req.userId() == null || req.userId().isBlank())
            ? ("dev-" + email.replaceAll("[^a-zA-Z0-9]", "_"))
            : req.userId().trim();

    var roles = Set.of("user", "dev"); // minimal roles
    String token = jwt.issue(userId, email, roles); // uses your existing JwtService
    var fp = Integer.toHexString(token.hashCode());
    log.info("issued dev jwt for {} (fp={})", email, fp.length() > 12 ? fp.substring(0,12) : fp);
    return ResponseEntity.ok(Map.of(
            "jwt", token,
            "userId", userId,
            "email", email,
            "roles", roles
    ));
  }
  // dev auth endpoint end

  @PostMapping("/google")
  public ResponseEntity<TokenRes> google(@RequestBody IdTokenReq req) throws Exception {
    var profile = google.verify(req.idToken());
    var token = jwt.issue(profile.userId(), profile.email(), Set.of("USER"));
    return ResponseEntity.ok(new TokenRes(token));
  }

  @PostMapping("/apple")
  public ResponseEntity<TokenRes> apple(@RequestBody IdTokenReq req) throws Exception {
    var profile = apple.verify(req.idToken());
    var token = jwt.issue(profile.userId(), profile.email(), Set.of("USER"));
    return ResponseEntity.ok(new TokenRes(token));
  }

  @GetMapping("/me")
  public Map<String, Object> me(@AuthenticationPrincipal AppPrincipal me) {
    return Map.of(
            "userId", me != null ? me.userId() : null,
            "email",  me != null ? me.email()  : null,
            "roles",  me != null ? me.roles()  : Set.of()
    );
  }
// me endpoint end

}
// --- AuthController end ---
