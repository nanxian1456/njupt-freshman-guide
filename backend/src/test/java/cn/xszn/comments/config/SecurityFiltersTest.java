package cn.xszn.comments.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityFiltersTest {
  private static final String ADMIN_TOKEN = "admin-token-that-is-at-least-thirty-two-characters";
  private final CommentProperties properties = new CommentProperties(
      ADMIN_TOKEN,
      "ip-secret-that-is-at-least-thirty-two-characters",
      "form-secret-that-is-at-least-thirty-two-characters",
      "https://www.xszn-ny.cn");

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void rejectsUnsafeRequestsFromOtherOrigins() throws ServletException, IOException {
    var request = new MockHttpServletRequest("POST", "/api/comments");
    request.addHeader("Origin", "https://attacker.example");
    var response = new MockHttpServletResponse();

    new SameOriginFilter(properties).doFilter(request, response, new MockFilterChain());

    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(response.getContentAsString()).contains("请求来源未获允许");
  }

  @Test
  void acceptsUnsafeRequestsFromConfiguredOrigin() throws ServletException, IOException {
    var request = new MockHttpServletRequest("POST", "/api/comments");
    request.addHeader("Origin", "https://www.xszn-ny.cn");
    var response = new MockHttpServletResponse();

    new SameOriginFilter(properties).doFilter(request, response, new MockFilterChain());

    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void rejectsInvalidAdminTokens() throws ServletException, IOException {
    var request = new MockHttpServletRequest("GET", "/api/admin/comments");
    request.addHeader("X-Admin-Token", "wrong-token");
    var response = new MockHttpServletResponse();

    new AdminTokenFilter(properties).doFilter(request, response, new MockFilterChain());

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void authenticatesValidAdminTokens() throws ServletException, IOException {
    var request = new MockHttpServletRequest("GET", "/api/admin/comments");
    request.addHeader("X-Admin-Token", ADMIN_TOKEN);
    var response = new MockHttpServletResponse();

    new AdminTokenFilter(properties).doFilter(request, response, new MockFilterChain());

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("comment-admin");
  }
}
