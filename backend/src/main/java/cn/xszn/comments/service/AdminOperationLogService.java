package cn.xszn.comments.service;

import cn.xszn.comments.dto.AdminOperationLogPageResponse;
import cn.xszn.comments.dto.AdminOperationLogResponse;
import cn.xszn.comments.model.AdminOperationLog;
import cn.xszn.comments.model.Comment;
import cn.xszn.comments.model.CommentStatus;
import cn.xszn.comments.repository.AdminOperationLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOperationLogService {
  private static final int PAGE_SIZE = 100;
  private static final String ACTOR = "comment-admin";
  private final AdminOperationLogRepository repository;

  public AdminOperationLogService(AdminOperationLogRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public void recordComment(
      String action,
      Comment comment,
      CommentStatus before,
      CommentStatus after) {
    String summary = comment.getTargetType() + ":" + comment.getTargetKey()
        + " / " + comment.getNickname()
        + " / " + excerpt(comment.getContent())
        + " / " + before + " -> " + (after == null ? "DELETED" : after);
    record(action, "COMMENT", Long.toString(comment.getId()), summary);
  }

  @Transactional
  public void record(
      String action,
      String entityType,
      String entityKey,
      String summary) {
    repository.save(new AdminOperationLog(
        action,
        entityType,
        entityKey,
        limit(summary, 500),
        ACTOR));
  }

  @Transactional(readOnly = true)
  public AdminOperationLogPageResponse list(int page) {
    int safePage = Math.max(0, Math.min(page, 1000));
    var result = repository.findAllByOrderByCreatedAtDescIdDesc(
        PageRequest.of(safePage, PAGE_SIZE));
    return new AdminOperationLogPageResponse(
        result.getContent().stream().map(AdminOperationLogResponse::from).toList(),
        safePage,
        result.hasNext());
  }

  private String excerpt(String value) {
    return limit(value == null ? "" : value.replaceAll("\\s+", " ").trim(), 100);
  }

  private String limit(String value, int maxLength) {
    if (value.length() <= maxLength) return value;
    return value.substring(0, maxLength - 1) + "…";
  }
}
