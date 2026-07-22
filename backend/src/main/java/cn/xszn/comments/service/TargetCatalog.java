package cn.xszn.comments.service;

import cn.xszn.comments.dto.TargetCatalogEntryResponse;
import cn.xszn.comments.dto.UpdateTargetCatalogRequest;
import cn.xszn.comments.model.TargetType;
import cn.xszn.comments.repository.TargetCatalogRepository;
import java.text.Normalizer;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TargetCatalog {
  private final TargetCatalogRepository repository;
  private final AdminOperationLogService operationLogService;

  public TargetCatalog(
      TargetCatalogRepository repository,
      AdminOperationLogService operationLogService) {
    this.repository = repository;
    this.operationLogService = operationLogService;
  }

  @Transactional(readOnly = true)
  public String requireName(TargetType type, String key) {
    var entry = repository.findByTargetTypeAndTargetKey(type, key)
        .filter(item -> item.isEnabled())
        .orElseThrow(() -> new InvalidCommentException("评价对象不存在或已停用"));
    return entry.getName();
  }

  @Transactional(readOnly = true)
  public String displayName(TargetType type, String key) {
    return repository.findByTargetTypeAndTargetKey(type, key)
        .map(item -> item.getName())
        .orElse(key);
  }

  @Transactional(readOnly = true)
  public List<TargetCatalogEntryResponse> listPublic(TargetType type) {
    return repository.findByTargetTypeAndEnabledTrueOrderBySortOrderAscTargetKeyAsc(type).stream()
        .map(TargetCatalogEntryResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<TargetCatalogEntryResponse> listAdmin() {
    return repository.findAllByOrderByTargetTypeAscSortOrderAscTargetKeyAsc().stream()
        .map(TargetCatalogEntryResponse::from)
        .toList();
  }

  @Transactional
  public TargetCatalogEntryResponse update(
      TargetType type,
      String key,
      UpdateTargetCatalogRequest request) {
    var entry = repository.findByTargetTypeAndTargetKey(type, key)
        .orElseThrow(() -> new InvalidCommentException("目录项目不存在"));
    String name = normalize(request.name());
    String groupName = normalize(request.groupName());
    if (name.isBlank() || groupName.isBlank()) {
      throw new InvalidCommentException("名称和分组不能为空");
    }
    String before = entry.getName() + " / " + entry.getGroupName()
        + " / " + entry.getSortOrder() + " / " + entry.isEnabled();
    entry.update(name, groupName, request.sortOrder(), request.enabled());
    operationLogService.record(
        "CATALOG_UPDATE",
        "TARGET",
        type + ":" + key,
        before + " -> " + name + " / " + groupName
            + " / " + request.sortOrder() + " / " + request.enabled());
    return TargetCatalogEntryResponse.from(entry);
  }

  private String normalize(String value) {
    if (value == null) return "";
    return Normalizer.normalize(value, Normalizer.Form.NFKC)
        .replaceAll("[\\p{Cc}]", "")
        .replaceAll("\\s+", " ")
        .trim();
  }
}
