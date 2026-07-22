package cn.xszn.comments.web;

import cn.xszn.comments.dto.CommentPageResponse;
import cn.xszn.comments.dto.CommentResponse;
import cn.xszn.comments.dto.ModerateCommentRequest;
import cn.xszn.comments.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/comments")
public class AdminCommentController {
  private final CommentService commentService;

  public AdminCommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @GetMapping
  public ResponseEntity<CommentPageResponse> pending(
      @RequestParam(defaultValue = "0") @Min(0) @Max(1000) int page) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(commentService.getPending(page));
  }

  @GetMapping("/managed")
  public ResponseEntity<CommentPageResponse> managed(
      @RequestParam(defaultValue = "0") @Min(0) @Max(1000) int page) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(commentService.getManaged(page));
  }

  @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<CommentResponse> moderate(
      @PathVariable long id, @Valid @RequestBody ModerateCommentRequest request) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(commentService.moderate(id, request.action(), request.confirmation()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable long id,
      @RequestHeader("X-Confirm-Comment-Id") String confirmation) {
    commentService.deleteManaged(id, confirmation);
    return ResponseEntity.noContent().build();
  }
}
