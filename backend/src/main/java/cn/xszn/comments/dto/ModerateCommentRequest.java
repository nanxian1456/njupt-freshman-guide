package cn.xszn.comments.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModerateCommentRequest(
    @NotNull ModerationAction action,
    @Size(max = 100) String confirmation) {
  public enum ModerationAction {
    APPROVE,
    REJECT,
    WITHDRAW,
    REPUBLISH
  }
}
