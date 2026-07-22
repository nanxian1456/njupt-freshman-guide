package cn.xszn.comments.web;

import cn.xszn.comments.dto.TargetCatalogResponse;
import cn.xszn.comments.model.TargetType;
import cn.xszn.comments.service.TargetCatalog;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
  private final TargetCatalog targetCatalog;

  public CatalogController(TargetCatalog targetCatalog) {
    this.targetCatalog = targetCatalog;
  }

  @GetMapping
  public ResponseEntity<TargetCatalogResponse> list(@RequestParam TargetType type) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(new TargetCatalogResponse(targetCatalog.listPublic(type)));
  }
}
