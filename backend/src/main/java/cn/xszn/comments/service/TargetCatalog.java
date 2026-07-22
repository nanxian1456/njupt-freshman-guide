package cn.xszn.comments.service;

import cn.xszn.comments.model.TargetType;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TargetCatalog {
  private static final Map<String, String> DORMS = Map.ofEntries(
      Map.entry("lanyuan", "兰苑"),
      Map.entry("meiyuan", "梅苑"),
      Map.entry("zhuyuan", "竹苑"),
      Map.entry("juyuan", "菊苑"),
      Map.entry("taoyuan", "桃苑"),
      Map.entry("liyuan", "李苑"),
      Map.entry("liuyuan", "柳苑"),
      Map.entry("guiyuan", "桂苑"),
      Map.entry("nanhe", "南荷"),
      Map.entry("beihe", "北荷"),
      Map.entry("yingyuan", "樱苑"),
      Map.entry("qingjiao", "青教"));

  private static final Map<String, String> FOODS = Map.ofEntries(
      Map.entry("xianlin-canteen-1", "仙林一食堂"),
      Map.entry("xianlin-canteen-2", "仙林二食堂"),
      Map.entry("xianlin-canteen-3", "仙林三食堂"),
      Map.entry("xianlin-outside", "仙林校外美食"),
      Map.entry("sanpailou-canteen", "三牌楼学生食堂"),
      Map.entry("sanpailou-outside", "三牌楼周边餐饮"));

  public String requireName(TargetType type, String key) {
    String name = switch (type) {
      case DORM -> DORMS.get(key);
      case FOOD -> FOODS.get(key);
    };
    if (name == null) {
      throw new InvalidCommentException("评价对象不存在");
    }
    return name;
  }
}
