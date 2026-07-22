package cn.xszn.comments.service;

import cn.xszn.comments.config.CommentProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class HmacService {
  private static final long TOKEN_MIN_AGE_SECONDS = 3;
  private static final long TOKEN_MAX_AGE_SECONDS = 2 * 60 * 60;
  private final byte[] ipSecret;
  private final byte[] formSecret;
  private final SecureRandom secureRandom = new SecureRandom();

  public HmacService(CommentProperties properties) {
    this.ipSecret = properties.ipHmacSecret().getBytes(StandardCharsets.UTF_8);
    this.formSecret = properties.formHmacSecret().getBytes(StandardCharsets.UTF_8);
  }

  public String hashIp(String ipAddress) {
    return hashIdentifier("ip:" + ipAddress);
  }

  public String hashIdentifier(String value) {
    return toHex(hmac(ipSecret, value));
  }

  public String issueFormToken() {
    long timestamp = Instant.now().getEpochSecond();
    byte[] nonce = new byte[18];
    secureRandom.nextBytes(nonce);
    String payload = timestamp + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(nonce);
    String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(hmac(formSecret, payload));
    return payload + "." + signature;
  }

  public void verifyFormToken(String token) {
    try {
      String[] parts = token.split("\\.", -1);
      if (parts.length != 3 || parts[1].length() < 20) {
        throw new InvalidCommentException("表单令牌无效，请刷新后重试");
      }
      long timestamp = Long.parseLong(parts[0]);
      long age = Instant.now().getEpochSecond() - timestamp;
      if (age < TOKEN_MIN_AGE_SECONDS) {
        throw new InvalidCommentException("提交过快，请稍后重试");
      }
      if (age > TOKEN_MAX_AGE_SECONDS || age < 0) {
        throw new InvalidCommentException("表单已过期，请刷新后重试");
      }
      String payload = parts[0] + "." + parts[1];
      byte[] expected = hmac(formSecret, payload);
      byte[] supplied = Base64.getUrlDecoder().decode(parts[2]);
      if (!MessageDigest.isEqual(expected, supplied)) {
        throw new InvalidCommentException("表单令牌无效，请刷新后重试");
      }
    } catch (IllegalArgumentException ex) {
      throw new InvalidCommentException("表单令牌无效，请刷新后重试");
    }
  }

  public long formTokenLifetimeSeconds() {
    return TOKEN_MAX_AGE_SECONDS;
  }

  private byte[] hmac(byte[] secret, String value) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("HMAC initialization failed", ex);
    }
  }

  private static String toHex(byte[] bytes) {
    StringBuilder result = new StringBuilder(bytes.length * 2);
    for (byte value : bytes) {
      result.append(String.format("%02x", value));
    }
    return result.toString();
  }
}
