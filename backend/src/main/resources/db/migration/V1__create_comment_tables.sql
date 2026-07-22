CREATE TABLE comments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  target_type VARCHAR(16) NOT NULL,
  target_key VARCHAR(64) NOT NULL,
  nickname VARCHAR(20) NOT NULL,
  content VARCHAR(300) NOT NULL,
  status VARCHAR(16) NOT NULL,
  ip_hash CHAR(64) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  reviewed_at DATETIME(3) NULL,
  PRIMARY KEY (id),
  INDEX idx_comments_public (target_type, target_key, status, created_at, id),
  INDEX idx_comments_moderation (status, created_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE comment_rate_limits (
  ip_hash CHAR(64) NOT NULL,
  window_started_at DATETIME(3) NOT NULL,
  submission_count INT NOT NULL,
  last_submitted_at DATETIME(3) NOT NULL,
  PRIMARY KEY (ip_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
