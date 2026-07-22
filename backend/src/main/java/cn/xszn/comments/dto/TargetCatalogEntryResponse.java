package cn.xszn.comments.dto;

import cn.xszn.comments.model.TargetCatalogEntry;
import cn.xszn.comments.model.TargetType;
import java.time.LocalDateTime;

public record TargetCatalogEntryResponse(
    TargetType targetType,
    String targetKey,
    String name,
    String groupName,
    int sortOrder,
    boolean enabled,
    LocalDateTime updatedAt) {
  public static TargetCatalogEntryResponse from(TargetCatalogEntry entry) {
    return new TargetCatalogEntryResponse(
        entry.getTargetType(),
        entry.getTargetKey(),
        entry.getName(),
        entry.getGroupName(),
        entry.getSortOrder(),
        entry.isEnabled(),
        entry.getUpdatedAt());
  }
}
