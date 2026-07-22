package cn.xszn.comments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cn.xszn.comments.model.TargetCatalogEntry;
import cn.xszn.comments.model.TargetType;
import cn.xszn.comments.repository.TargetCatalogRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TargetCatalogTest {
  private final TargetCatalogRepository repository = mock(TargetCatalogRepository.class);
  private final TargetCatalog catalog = new TargetCatalog(
      repository, mock(AdminOperationLogService.class));

  @BeforeEach
  void setUp() {
    when(repository.findByTargetTypeAndTargetKey(TargetType.DORM, "lanyuan"))
        .thenReturn(Optional.of(new TargetCatalogEntry(
            TargetType.DORM, "lanyuan", "兰苑", "传统苑区", 10, true)));
    when(repository.findByTargetTypeAndTargetKey(TargetType.FOOD, "sanpailou-canteen"))
        .thenReturn(Optional.of(new TargetCatalogEntry(
            TargetType.FOOD,
            "sanpailou-canteen",
            "三牌楼学生食堂",
            "三牌楼校区",
            50,
            true)));
  }

  @Test
  void resolvesKnownDormAndFoodTargets() {
    assertThat(catalog.requireName(TargetType.DORM, "lanyuan")).isEqualTo("兰苑");
    assertThat(catalog.requireName(TargetType.FOOD, "sanpailou-canteen")).isEqualTo("三牌楼学生食堂");
  }

  @Test
  void rejectsUnknownTargets() {
    assertThatThrownBy(() -> catalog.requireName(TargetType.DORM, "unknown"))
        .isInstanceOf(InvalidCommentException.class);
  }
}
