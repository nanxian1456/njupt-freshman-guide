package cn.xszn.comments.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "target_catalog",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_target_catalog_type_key",
        columnNames = {"target_type", "target_key"}))
public class TargetCatalogEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 16)
  private TargetType targetType;

  @Column(name = "target_key", nullable = false, length = 64)
  private String targetKey;

  @Column(nullable = false, length = 40)
  private String name;

  @Column(name = "group_name", nullable = false, length = 40)
  private String groupName;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Column(nullable = false)
  private boolean enabled;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  protected TargetCatalogEntry() {}

  public TargetCatalogEntry(
      TargetType targetType,
      String targetKey,
      String name,
      String groupName,
      int sortOrder,
      boolean enabled) {
    this.targetType = targetType;
    this.targetKey = targetKey;
    update(name, groupName, sortOrder, enabled);
  }

  public void update(String name, String groupName, int sortOrder, boolean enabled) {
    this.name = name;
    this.groupName = groupName;
    this.sortOrder = sortOrder;
    this.enabled = enabled;
    this.updatedAt = LocalDateTime.now();
  }

  public Long getId() { return id; }
  public TargetType getTargetType() { return targetType; }
  public String getTargetKey() { return targetKey; }
  public String getName() { return name; }
  public String getGroupName() { return groupName; }
  public int getSortOrder() { return sortOrder; }
  public boolean isEnabled() { return enabled; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }
}
