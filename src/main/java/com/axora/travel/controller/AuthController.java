// --- AuthController start ---
package com.axora.travel.controller;

import com.axora.travel.security.GoogleTokenVerifier;
import com.axora.travel.security.AppleTokenVerifier;
import com.axora.travel.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins="*")
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
  @org.springframework.beans.factory.annotation.Value("${security.devAuthEnabled:false}")
  private boolean devAuthEnabled;

  public record DevReq(String email, String userId, java.util.Set<String> roles) {}

  @PostMapping("/dev")
  public ResponseEntity<?> dev(@RequestBody DevReq req) {
    if (!devAuthEnabled) {
      return ResponseEntity.status(403).body(java.util.Map.of("error", "dev_auth_disabled"));
    }
    String email = (req.email() == null || req.email().isBlank()) ? "tester@example.com" : req.email().trim();
    String userId = (req.userId() == null || req.userId().isBlank())
        ? ("dev-" + email.replaceAll("[^a-zA-Z0-9]", "_"))
        : req.userId().trim();
    java.util.Set<String> roles = (req.roles() == null || req.roles().isEmpty())
        ? java.util.Set.of("user", "dev")
        : req.roles();

    String token = jwt.issue(userId, email, roles);  // uses your existing JwtService
    return ResponseEntity.ok(java.util.Map.of(
        "token", token,
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
  public java.util.Map<String,Object> me(
      @org.springframework.security.core.annotation.AuthenticationPrincipal
      com.axora.travel.security.AppPrincipal me) {
    if (me == null) return java.util.Map.of("auth", "none");
    return java.util.Map.of("email", me.email(), "userId", me.userId(), "roles", me.roles());
  }
}
// --- AuthController end ---
