package cn.xszn.comments.web;

import cn.xszn.comments.dto.AdminOperationLogPageResponse;
import cn.xszn.comments.service.AdminOperationLogService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/operations")
public class AdminOperationController {
  private final AdminOperationLogService operationLogService;

  public AdminOperationController(AdminOperationLogService operationLogService) {
    this.operationLogService = operationLogService;
  }

  @GetMapping
  public ResponseEntity<AdminOperationLogPageResponse> list(
      @RequestParam(defaultValue = "0") @Min(0) @Max(1000) int page) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(operationLogService.list(page));
  }
}
