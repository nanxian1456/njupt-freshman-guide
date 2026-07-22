package cn.xszn.comments.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.xszn.comments.dto.ModerateCommentRequest.ModerationAction;
import cn.xszn.comments.model.Comment;
import cn.xszn.comments.model.CommentStatus;
import cn.xszn.comments.model.TargetType;
import cn.xszn.comments.repository.CommentRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CommentServiceAdminTest {
  private final CommentRepository repository = mock(CommentRepository.class);
  private final TargetCatalog targetCatalog = mock(TargetCatalog.class);
  private final AdminOperationLogService operationLogService = mock(AdminOperationLogService.class);
  private final CommentService service = new CommentService(
      repository,
      targetCatalog,
      mock(HmacService.class),
      mock(CommentRateLimiter.class),
      operationLogService);

  @Test
  void withdrawRequiresMatchingSecondConfirmation() {
    Comment comment = mock(Comment.class);
    when(comment.getId()).thenReturn(42L);
    when(comment.getStatus()).thenReturn(CommentStatus.APPROVED);
    when(repository.findById(42L)).thenReturn(Optional.of(comment));

    assertThatThrownBy(() -> service.moderate(42L, ModerationAction.WITHDRAW, "wrong"))
        .isInstanceOf(InvalidCommentException.class)
        .hasMessageContaining("二次确认");
  }

  @Test
  void withdrawsApprovedCommentAndRecordsOperation() {
    Comment comment = mock(Comment.class);
    when(comment.getId()).thenReturn(42L);
    when(comment.getStatus()).thenReturn(CommentStatus.APPROVED);
    when(comment.getTargetType()).thenReturn(TargetType.DORM);
    when(comment.getTargetKey()).thenReturn("lanyuan");
    when(targetCatalog.displayName(TargetType.DORM, "lanyuan")).thenReturn("兰苑");
    when(repository.findById(42L)).thenReturn(Optional.of(comment));

    service.moderate(42L, ModerationAction.WITHDRAW, "WITHDRAW:42");

    verify(comment).moderate(CommentStatus.WITHDRAWN);
    verify(operationLogService).recordComment(
        "COMMENT_WITHDRAW", comment, CommentStatus.APPROVED, CommentStatus.WITHDRAWN);
  }

  @Test
  void permanentlyDeletesOnlyWithMatchingId() {
    Comment comment = mock(Comment.class);
    when(comment.getId()).thenReturn(42L);
    when(comment.getStatus()).thenReturn(CommentStatus.WITHDRAWN);
    when(repository.findById(42L)).thenReturn(Optional.of(comment));

    service.deleteManaged(42L, "42");

    verify(operationLogService).recordComment(
        "COMMENT_DELETE", comment, CommentStatus.WITHDRAWN, null);
    verify(repository).delete(comment);
  }
}
