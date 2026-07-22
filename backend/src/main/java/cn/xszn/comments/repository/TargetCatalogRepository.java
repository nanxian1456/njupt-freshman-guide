package cn.xszn.comments.repository;

import cn.xszn.comments.model.TargetCatalogEntry;
import cn.xszn.comments.model.TargetType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TargetCatalogRepository extends JpaRepository<TargetCatalogEntry, Long> {
  Optional<TargetCatalogEntry> findByTargetTypeAndTargetKey(TargetType targetType, String targetKey);

  List<TargetCatalogEntry> findByTargetTypeAndEnabledTrueOrderBySortOrderAscTargetKeyAsc(
      TargetType targetType);

  List<TargetCatalogEntry> findAllByOrderByTargetTypeAscSortOrderAscTargetKeyAsc();
}
