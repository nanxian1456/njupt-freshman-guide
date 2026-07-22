package cn.xszn.comments.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.xszn.comments.dto.CreateCommentRequest;
import cn.xszn.comments.model.Comment;
import cn.xszn.comments.model.TargetType;
import cn.xszn.comments.repository.CommentRepository;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class CommentServiceRateLimitTest {
  @Test
  void appliesSharedIpAndBrowserSessionLimits() {
    CommentRepository repository = mock(CommentRepository.class);
    TargetCatalog targetCatalog = mock(TargetCatalog.class);
    HmacService hmacService = mock(HmacService.class);
    CommentRateLimiter rateLimiter = mock(CommentRateLimiter.class);
    AdminOperationLogService operationLogService = mock(AdminOperationLogService.class);
    CommentService service = new CommentService(
        repository, targetCatalog, hmacService, rateLimiter, operationLogService);

    when(hmacService.hashIp("203.0.113.10")).thenReturn("ip-hash");
    when(hmacService.hashIdentifier("client:203.0.113.10|browser-session-id"))
        .thenReturn("client-hash");
    Comment savedComment = mock(Comment.class);
    when(savedComment.getId()).thenReturn(42L);
    when(repository.save(org.mockito.ArgumentMatchers.any(Comment.class))).thenReturn(savedComment);

    service.create(
        new CreateCommentRequest(
            TargetType.DORM, "lanyuan", "同学", "这是一条测试评价", "form-token", ""),
        "203.0.113.10",
        "browser-session-id");

    verify(rateLimiter).checkAndRecord(
        "ip-hash", 30, Duration.ofMinutes(10), Duration.ofSeconds(1));
    verify(rateLimiter).checkAndRecord(
        "client-hash", 3, Duration.ofMinutes(10), Duration.ofSeconds(20));
  }
}
