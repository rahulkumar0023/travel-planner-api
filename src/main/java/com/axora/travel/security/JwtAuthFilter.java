// --- JwtAuthFilter start ---
package com.axora.travel.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwt;

  public JwtAuthFilter(JwtService jwt) { this.jwt = jwt; }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String h = req.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION);
    if (h == null) {
      org.slf4j.LoggerFactory.getLogger(getClass()).debug("Auth: no Authorization header");
    } else {
      org.slf4j.LoggerFactory.getLogger(getClass()).debug("Auth: header present ({} chars)", h.length());
    }

    if (h != null && h.startsWith("Bearer ")) {
      try {
        DecodedJWT d = jwt.verify(h.substring(7));
        org.slf4j.LoggerFactory.getLogger(getClass()).info(
            "JWT OK: sub={}, email={}, roles={}",
            d.getSubject(), d.getClaim("email").asString(),
            java.util.Arrays.toString(d.getClaim("roles").asArray(String.class))
        );
        String userId = d.getSubject();
        String email = d.getClaim("email").asString();
        String[] roles = d.getClaim("roles").asArray(String.class);
        Set<GrantedAuthority> auths = roles == null ? Set.of() :
            Arrays.stream(roles).map(r -> new SimpleGrantedAuthority("ROLE_" + r)).collect(Collectors.toSet());

        AppPrincipal principal = new AppPrincipal(userId, email,
            roles == null ? Set.of() : Set.of(roles));

        AbstractAuthenticationToken at = new AbstractAuthenticationToken(auths) {
          @Override public Object getCredentials() { return null; }
          @Override public Object getPrincipal() { return principal; }
        };
        at.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(at);
      } catch (Exception e) {
        org.slf4j.LoggerFactory.getLogger(getClass()).warn("JWT invalid: {}", e.getMessage());
        // Leave unauthenticated; downstream may 401.
      }
    }
    chain.doFilter(req, res);
  }
}
// --- JwtAuthFilter end ---
