package cn.xszn.comments.web;

import cn.xszn.comments.dto.TargetCatalogEntryResponse;
import cn.xszn.comments.dto.TargetCatalogResponse;
import cn.xszn.comments.dto.UpdateTargetCatalogRequest;
import cn.xszn.comments.model.TargetType;
import cn.xszn.comments.service.TargetCatalog;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/catalog")
public class AdminCatalogController {
  private final TargetCatalog targetCatalog;

  public AdminCatalogController(TargetCatalog targetCatalog) {
    this.targetCatalog = targetCatalog;
  }

  @GetMapping
  public ResponseEntity<TargetCatalogResponse> list() {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(new TargetCatalogResponse(targetCatalog.listAdmin()));
  }

  @PatchMapping("/{type}/{key}")
  public ResponseEntity<TargetCatalogEntryResponse> update(
      @PathVariable TargetType type,
      @PathVariable @Pattern(regexp = "[a-z0-9-]{2,64}") String key,
      @Valid @RequestBody UpdateTargetCatalogRequest request) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(targetCatalog.update(type, key, request));
  }
}
