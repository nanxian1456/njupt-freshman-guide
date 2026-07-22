package cn.xszn.comments.service;

import cn.xszn.comments.dto.CommentPageResponse;
import cn.xszn.comments.dto.CommentResponse;
import cn.xszn.comments.dto.CreateCommentRequest;
import cn.xszn.comments.dto.CreateCommentResponse;
import cn.xszn.comments.dto.ModerateCommentRequest.ModerationAction;
import cn.xszn.comments.model.Comment;
import cn.xszn.comments.model.CommentStatus;
import cn.xszn.comments.model.TargetType;
import cn.xszn.comments.repository.CommentRepository;
import java.text.Normalizer;
import java.time.Duration;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
  private static final int PUBLIC_PAGE_SIZE = 20;
  private static final int ADMIN_PAGE_SIZE = 100;
  private final CommentRepository repository;
  private final TargetCatalog targetCatalog;
  private final HmacService hmacService;
  private final CommentRateLimiter rateLimiter;
  private final AdminOperationLogService operationLogService;

  public CommentService(
      CommentRepository repository,
      TargetCatalog targetCatalog,
      HmacService hmacService,
      CommentRateLimiter rateLimiter,
      AdminOperationLogService operationLogService) {
    this.repository = repository;
    this.targetCatalog = targetCatalog;
    this.hmacService = hmacService;
    this.rateLimiter = rateLimiter;
    this.operationLogService = operationLogService;
  }

  @Transactional(readOnly = true)
  public CommentPageResponse getApproved(TargetType type, String key, int page) {
    String targetName = targetCatalog.requireName(type, key);
    int safePage = Math.max(0, Math.min(page, 100));
    var result = repository.findByTargetTypeAndTargetKeyAndStatusOrderByCreatedAtDescIdDesc(
        type, key, CommentStatus.APPROVED, PageRequest.of(safePage, PUBLIC_PAGE_SIZE));
    var comments = result.getContent().stream()
        .map(comment -> CommentResponse.from(comment, targetName, false))
        .toList();
    return new CommentPageResponse(comments, safePage, result.hasNext());
  }

  @Transactional
  public CreateCommentResponse create(CreateCommentRequest request, String ipAddress, String clientId) {
    targetCatalog.requireName(request.targetType(), request.targetKey());
    hmacService.verifyFormToken(request.formToken());
    String ipHash = hmacService.hashIp(ipAddress);
    String clientHash = hmacService.hashIdentifier("client:" + ipAddress + "|" + clientId);
    rateLimiter.checkAndRecord(ipHash, 30, Duration.ofMinutes(10), Duration.ofSeconds(1));
    rateLimiter.checkAndRecord(clientHash, 3, Duration.ofMinutes(10), Duration.ofSeconds(20));

    String nickname = normalize(request.nickname());
    if (nickname.isBlank()) nickname = "匿名同学";
    String content = normalize(request.content());
    if (nickname.length() > 20 || content.length() < 5 || content.length() > 300) {
      throw new InvalidCommentException("昵称或评价长度不符合要求");
    }

    Comment saved = repository.save(new Comment(
        request.targetType(), request.targetKey(), nickname, content, ipHash));
    return new CreateCommentResponse(saved.getId(), "PENDING", "已提交，审核通过后公开显示");
  }

  @Transactional(readOnly = true)
  public CommentPageResponse getPending(int page) {
    int safePage = Math.max(0, Math.min(page, 1000));
    var result = repository.findByStatusOrderByCreatedAtAscIdAsc(
        CommentStatus.PENDING, PageRequest.of(safePage, ADMIN_PAGE_SIZE));
    var comments = result.getContent().stream()
        .map(comment -> CommentResponse.from(
            comment, targetCatalog.displayName(comment.getTargetType(), comment.getTargetKey()), true))
        .toList();
    return new CommentPageResponse(comments, safePage, result.hasNext());
  }

  @Transactional(readOnly = true)
  public CommentPageResponse getManaged(int page) {
    int safePage = Math.max(0, Math.min(page, 1000));
    var result = repository.findByStatusInOrderByReviewedAtDescIdDesc(
        List.of(CommentStatus.APPROVED, CommentStatus.WITHDRAWN),
        PageRequest.of(safePage, ADMIN_PAGE_SIZE));
    var comments = result.getContent().stream()
        .map(comment -> CommentResponse.from(
            comment, targetCatalog.displayName(comment.getTargetType(), comment.getTargetKey()), true))
        .toList();
    return new CommentPageResponse(comments, safePage, result.hasNext());
  }

  @Transactional
  public CommentResponse moderate(long id, ModerationAction action, String confirmation) {
    Comment comment = repository.findById(id)
        .orElseThrow(() -> new InvalidCommentException("评价不存在"));
    CommentStatus before = comment.getStatus();
    CommentStatus next = switch (action) {
      case APPROVE -> requireTransition(before, CommentStatus.PENDING, CommentStatus.APPROVED);
      case REJECT -> requireTransition(before, CommentStatus.PENDING, CommentStatus.REJECTED);
      case WITHDRAW -> {
        requireConfirmation(confirmation, "WITHDRAW:" + id);
        yield requireTransition(before, CommentStatus.APPROVED, CommentStatus.WITHDRAWN);
      }
      case REPUBLISH -> {
        requireConfirmation(confirmation, "REPUBLISH:" + id);
        yield requireTransition(before, CommentStatus.WITHDRAWN, CommentStatus.APPROVED);
      }
    };
    comment.moderate(next);
    operationLogService.recordComment("COMMENT_" + action, comment, before, next);
    return CommentResponse.from(
        comment, targetCatalog.displayName(comment.getTargetType(), comment.getTargetKey()), true);
  }

  @Transactional
  public void deleteManaged(long id, String confirmation) {
    requireConfirmation(confirmation, Long.toString(id));
    Comment comment = repository.findById(id)
        .orElseThrow(() -> new InvalidCommentException("评价不存在"));
    CommentStatus before = comment.getStatus();
    if (before != CommentStatus.APPROVED && before != CommentStatus.WITHDRAWN) {
      throw new InvalidCommentException("只能永久删除已发布或已撤回的评价");
    }
    operationLogService.recordComment("COMMENT_DELETE", comment, before, null);
    repository.delete(comment);
  }

  private CommentStatus requireTransition(
      CommentStatus current,
      CommentStatus required,
      CommentStatus next) {
    if (current != required) {
      throw new InvalidCommentException("当前评价状态不允许执行该操作");
    }
    return next;
  }

  private void requireConfirmation(String actual, String expected) {
    if (!expected.equals(actual)) {
      throw new InvalidCommentException("二次确认信息不正确");
    }
  }

  private String normalize(String value) {
    if (value == null) return "";
    String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .replaceAll("[\\p{Cc}&&[^\\n\\t]]", "")
        .replaceAll("[ \\t]+", " ")
        .replaceAll("\\n{3,}", "\n\n")
        .trim();
    return normalized;
  }
}
