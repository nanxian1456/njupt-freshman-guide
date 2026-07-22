package cn.xszn.comments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.xszn.comments.model.TargetType;
import org.junit.jupiter.api.Test;

class TargetCatalogTest {
  private final TargetCatalog catalog = new TargetCatalog();

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
