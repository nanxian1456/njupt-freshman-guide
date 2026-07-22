package cn.xszn.comments.dto;

import jakarta.validation.constraints.NotNull;

public record ModerateCommentRequest(@NotNull ModerationAction action) {
  public enum ModerationAction {
    APPROVE,
    REJECT
  }
}
