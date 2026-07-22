package cn.xszn.comments.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 16)
  private TargetType targetType;

  @Column(name = "target_key", nullable = false, length = 64)
  private String targetKey;

  @Column(nullable = false, length = 20)
  private String nickname;

  @Column(nullable = false, length = 300)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private CommentStatus status;

  @Column(name = "ip_hash", nullable = false, length = 64)
  private String ipHash;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "reviewed_at")
  private LocalDateTime reviewedAt;

  protected Comment() {}

  public Comment(TargetType targetType, String targetKey, String nickname, String content, String ipHash) {
    this.targetType = targetType;
    this.targetKey = targetKey;
    this.nickname = nickname;
    this.content = content;
    this.ipHash = ipHash;
    this.status = CommentStatus.PENDING;
    this.createdAt = LocalDateTime.now();
  }

  public void moderate(CommentStatus nextStatus) {
    this.status = nextStatus;
    this.reviewedAt = LocalDateTime.now();
  }

  public Long getId() { return id; }
  public TargetType getTargetType() { return targetType; }
  public String getTargetKey() { return targetKey; }
  public String getNickname() { return nickname; }
  public String getContent() { return content; }
  public CommentStatus getStatus() { return status; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public LocalDateTime getReviewedAt() { return reviewedAt; }
}
