// --- AppPrincipal start ---
package com.axora.travel.security;

import java.util.Set;

public record AppPrincipal(String userId, String email, Set<String> roles) {
  public boolean isAdmin() { return roles != null && roles.contains("admin"); }
}
// --- AppPrincipal end ---
