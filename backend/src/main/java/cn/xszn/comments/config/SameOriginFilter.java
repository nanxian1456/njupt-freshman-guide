package cn.xszn.comments.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SameOriginFilter extends OncePerRequestFilter {
  private final CommentProperties properties;

  public SameOriginFilter(CommentProperties properties) {
    this.properties = properties;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return switch (request.getMethod()) {
      case "POST", "PUT", "PATCH", "DELETE" -> false;
      default -> true;
    };
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String origin = request.getHeader("Origin");
    if (origin == null || !properties.allowedOrigins().contains(origin)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getWriter().write("{\"message\":\"请求来源未获允许\"}");
      return;
    }
    filterChain.doFilter(request, response);
  }
}
