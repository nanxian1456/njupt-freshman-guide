package cn.xszn.comments.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_operation_logs")
public class AdminOperationLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 40)
  private String action;

  @Column(name = "entity_type", nullable = false, length = 24)
  private String entityType;

  @Column(name = "entity_key", nullable = false, length = 80)
  private String entityKey;

  @Column(nullable = false, length = 500)
  private String summary;

  @Column(nullable = false, length = 40)
  private String actor;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  protected AdminOperationLog() {}

  public AdminOperationLog(
      String action,
      String entityType,
      String entityKey,
      String summary,
      String actor) {
    this.action = action;
    this.entityType = entityType;
    this.entityKey = entityKey;
    this.summary = summary;
    this.actor = actor;
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() { return id; }
  public String getAction() { return action; }
  public String getEntityType() { return entityType; }
  public String getEntityKey() { return entityKey; }
  public String getSummary() { return summary; }
  public String getActor() { return actor; }
  public LocalDateTime getCreatedAt() { return createdAt; }
}
