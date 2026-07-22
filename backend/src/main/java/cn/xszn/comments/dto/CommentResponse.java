package cn.xszn.comments.dto;

import cn.xszn.comments.model.Comment;
import cn.xszn.comments.model.CommentStatus;
import cn.xszn.comments.model.TargetType;
import java.time.LocalDateTime;

public record CommentResponse(
    long id,
    TargetType targetType,
    String targetKey,
    String targetName,
    String nickname,
    String content,
    CommentStatus status,
    LocalDateTime createdAt) {
  public static CommentResponse from(Comment comment, String targetName, boolean includeStatus) {
    return new CommentResponse(
        comment.getId(),
        comment.getTargetType(),
        comment.getTargetKey(),
        targetName,
        comment.getNickname(),
        comment.getContent(),
        includeStatus ? comment.getStatus() : null,
        comment.getCreatedAt());
  }
}
