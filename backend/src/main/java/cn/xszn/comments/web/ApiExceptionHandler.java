package cn.xszn.comments.web;

import cn.xszn.comments.service.InvalidCommentException;
import cn.xszn.comments.service.RateLimitException;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler({
      MethodArgumentNotValidException.class,
      ConstraintViolationException.class,
      HttpMessageNotReadableException.class
  })
  public ResponseEntity<Map<String, String>> validationError(Exception ignored) {
    return ResponseEntity.badRequest().body(Map.of("message", "提交内容格式不正确"));
  }

  @ExceptionHandler(InvalidCommentException.class)
  public ResponseEntity<Map<String, String>> invalidComment(InvalidCommentException ex) {
    return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
  }

  @ExceptionHandler(RateLimitException.class)
  public ResponseEntity<Map<String, String>> rateLimited(RateLimitException ex) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.RETRY_AFTER, Long.toString(ex.getRetryAfterSeconds()));
    return new ResponseEntity<>(Map.of("message", ex.getMessage()), headers, HttpStatus.TOO_MANY_REQUESTS);
  }
}
