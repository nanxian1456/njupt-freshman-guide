package cn.xszn.comments.web;

import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {
  private final JdbcTemplate jdbcTemplate;

  public HealthController(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @GetMapping
  public ResponseEntity<Map<String, String>> health() {
    Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
    String status = Integer.valueOf(1).equals(result) ? "ok" : "degraded";
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("status", status));
  }
}
