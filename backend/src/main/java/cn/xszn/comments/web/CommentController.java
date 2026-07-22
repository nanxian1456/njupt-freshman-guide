package cn.xszn.comments.web;

import cn.xszn.comments.dto.CommentPageResponse;
import cn.xszn.comments.dto.CreateCommentRequest;
import cn.xszn.comments.dto.CreateCommentResponse;
import cn.xszn.comments.dto.FormTokenResponse;
import cn.xszn.comments.model.TargetType;
import cn.xszn.comments.service.ClientIdentityService;
import cn.xszn.comments.service.CommentService;
import cn.xszn.comments.service.HmacService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/comments")
public class CommentController {
  private final CommentService commentService;
  private final ClientIdentityService clientIdentityService;
  private final HmacService hmacService;

  public CommentController(
      CommentService commentService,
      ClientIdentityService clientIdentityService,
      HmacService hmacService) {
    this.commentService = commentService;
    this.clientIdentityService = clientIdentityService;
    this.hmacService = hmacService;
  }

  @GetMapping
  public ResponseEntity<CommentPageResponse> list(
      @RequestParam TargetType targetType,
      @RequestParam @Pattern(regexp = "[a-z0-9-]{2,64}") String targetKey,
      @RequestParam(defaultValue = "0") @Min(0) @Max(100) int page) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(commentService.getApproved(targetType, targetKey, page));
  }

  @GetMapping("/form-token")
  public ResponseEntity<FormTokenResponse> formToken() {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(new FormTokenResponse(hmacService.issueFormToken(), hmacService.formTokenLifetimeSeconds()));
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<CreateCommentResponse> create(
      @Valid @RequestBody CreateCommentRequest request,
      @RequestHeader("X-Comment-Client") @Pattern(regexp = "[A-Za-z0-9_-]{20,64}") String clientId,
      HttpServletRequest servletRequest) {
    String clientIp = clientIdentityService.getClientIp(servletRequest);
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .cacheControl(CacheControl.noStore())
        .body(commentService.create(request, clientIp, clientId));
  }
}
