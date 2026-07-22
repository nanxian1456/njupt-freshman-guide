package cn.xszn.comments.config;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CommentProperties {
  private final String adminToken;
  private final String ipHmacSecret;
  private final String formHmacSecret;
  private final Set<String> allowedOrigins;

  public CommentProperties(
      @Value("${comments.admin-token}") String adminToken,
      @Value("${comments.ip-hmac-secret}") String ipHmacSecret,
      @Value("${comments.form-hmac-secret}") String formHmacSecret,
      @Value("${comments.allowed-origins}") String allowedOrigins) {
    this.adminToken = requireSecret(adminToken, "COMMENT_ADMIN_TOKEN");
    this.ipHmacSecret = requireSecret(ipHmacSecret, "COMMENT_IP_HMAC_SECRET");
    this.formHmacSecret = requireSecret(formHmacSecret, "COMMENT_FORM_HMAC_SECRET");
    this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .collect(Collectors.toUnmodifiableSet());
    if (this.allowedOrigins.isEmpty()) {
      throw new IllegalStateException("COMMENT_ALLOWED_ORIGINS must not be empty");
    }
  }

  private static String requireSecret(String value, String name) {
    if (value == null || value.length() < 32) {
      throw new IllegalStateException(name + " must contain at least 32 characters");
    }
    return value;
  }

  public String adminToken() { return adminToken; }
  public String ipHmacSecret() { return ipHmacSecret; }
  public String formHmacSecret() { return formHmacSecret; }
  public Set<String> allowedOrigins() { return allowedOrigins; }
}
