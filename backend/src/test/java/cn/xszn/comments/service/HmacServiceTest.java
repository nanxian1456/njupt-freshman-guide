package cn.xszn.comments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.xszn.comments.config.CommentProperties;
import org.junit.jupiter.api.Test;

class HmacServiceTest {
  private final HmacService service = new HmacService(new CommentProperties(
      "admin-token-that-is-at-least-thirty-two-characters",
      "ip-secret-that-is-at-least-thirty-two-characters",
      "form-secret-that-is-at-least-thirty-two-characters",
      "https://www.xszn-ny.cn"));

  @Test
  void hashesIpWithoutReturningTheAddress() {
    String hash = service.hashIp("203.0.113.8");
    assertThat(hash).hasSize(64).doesNotContain("203.0.113.8");
  }

  @Test
  void rejectsMalformedFormTokens() {
    assertThatThrownBy(() -> service.verifyFormToken("invalid"))
        .isInstanceOf(InvalidCommentException.class);
  }
}
