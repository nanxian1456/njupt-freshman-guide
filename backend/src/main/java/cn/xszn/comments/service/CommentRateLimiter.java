package cn.xszn.comments.service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentRateLimiter {
  private final JdbcTemplate jdbcTemplate;

  public CommentRateLimiter(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Transactional
  public synchronized void checkAndRecord(
      String rateLimitKey, int maxSubmissions, Duration window, Duration minInterval) {
    LocalDateTime now = LocalDateTime.now();
    List<RateState> states = jdbcTemplate.query(
        "SELECT window_started_at, submission_count, last_submitted_at "
            + "FROM comment_rate_limits WHERE ip_hash = ? FOR UPDATE",
        (rs, rowNum) -> new RateState(
            rs.getTimestamp("window_started_at").toLocalDateTime(),
            rs.getInt("submission_count"),
            rs.getTimestamp("last_submitted_at").toLocalDateTime()),
        rateLimitKey);

    if (states.isEmpty()) {
      jdbcTemplate.update(
          "INSERT INTO comment_rate_limits "
              + "(ip_hash, window_started_at, submission_count, last_submitted_at) VALUES (?, ?, 1, ?)",
          rateLimitKey, Timestamp.valueOf(now), Timestamp.valueOf(now));
      return;
    }

    RateState state = states.get(0);
    Duration sinceLast = Duration.between(state.lastSubmittedAt(), now);
    if (sinceLast.compareTo(minInterval) < 0) {
      throw new RateLimitException("提交过于频繁，请稍后再试", minInterval.minus(sinceLast).toSeconds() + 1);
    }

    Duration windowAge = Duration.between(state.windowStartedAt(), now);
    if (windowAge.compareTo(window) >= 0) {
      jdbcTemplate.update(
          "UPDATE comment_rate_limits SET window_started_at = ?, submission_count = 1, "
              + "last_submitted_at = ? WHERE ip_hash = ?",
          Timestamp.valueOf(now), Timestamp.valueOf(now), rateLimitKey);
      return;
    }

    if (state.submissionCount() >= maxSubmissions) {
      throw new RateLimitException("提交次数已达到当前时段上限", window.minus(windowAge).toSeconds() + 1);
    }
    jdbcTemplate.update(
        "UPDATE comment_rate_limits SET submission_count = submission_count + 1, "
            + "last_submitted_at = ? WHERE ip_hash = ?",
        Timestamp.valueOf(now), rateLimitKey);
  }

  private record RateState(LocalDateTime windowStartedAt, int submissionCount, LocalDateTime lastSubmittedAt) {}
}
