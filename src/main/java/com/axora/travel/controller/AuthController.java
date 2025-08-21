// --- AuthController start ---
package com.axora.travel.controller;

import com.axora.travel.security.AppleTokenVerifier;
import com.axora.travel.security.GoogleTokenVerifier;
import com.axora.travel.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {
  private final GoogleTokenVerifier google;
  private final AppleTokenVerifier apple;
  private final JwtService jwt;

  public AuthController(GoogleTokenVerifier google, AppleTokenVerifier apple, JwtService jwt) {
    this.google = google; this.apple = apple; this.jwt = jwt;
  }

  public record ExchangeReq(String provider, String idToken) {}
  @PostMapping("/exchange")
  public ResponseEntity<?> exchange(@RequestBody ExchangeReq req) throws Exception {
    String provider = req.provider() == null ? "" : req.provider().toLowerCase();
    String userId; String email;

    switch (provider) {
      case "google" -> {
        var p = google.verify(req.idToken());
        userId = (String)p.getSubject();
        email = (String)p.getEmail();
      }
      case "apple" -> {
        var c = apple.verify(req.idToken());
        userId = c.getSubject();
        email = (String)c.getClaim("email");
        if (email == null) email = userId + "@apple.local";
      }
      default -> { return ResponseEntity.badRequest().body(Map.of("error", "unknown_provider")); }
    }

    Set<String> roles = Set.of("user");
    String token = jwt.issue(userId, email, roles);
    return ResponseEntity.ok(Map.of("token", token, "userId", userId, "email", email, "roles", roles));
  }
}
// --- AuthController end ---
