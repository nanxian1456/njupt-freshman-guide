package cn.xszn.comments.dto;

import cn.xszn.comments.model.AdminOperationLog;
import java.time.LocalDateTime;

public record AdminOperationLogResponse(
    long id,
    String action,
    String entityType,
    String entityKey,
    String summary,
    String actor,
    LocalDateTime createdAt) {
  public static AdminOperationLogResponse from(AdminOperationLog log) {
    return new AdminOperationLogResponse(
        log.getId(),
        log.getAction(),
        log.getEntityType(),
        log.getEntityKey(),
        log.getSummary(),
        log.getActor(),
        log.getCreatedAt());
  }
}
