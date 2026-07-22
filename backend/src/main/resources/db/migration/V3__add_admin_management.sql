CREATE TABLE target_catalog (
  id BIGINT NOT NULL AUTO_INCREMENT,
  target_type VARCHAR(16) NOT NULL,
  target_key VARCHAR(64) NOT NULL,
  name VARCHAR(40) NOT NULL,
  group_name VARCHAR(40) NOT NULL,
  sort_order INT NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  updated_at DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_target_catalog_type_key UNIQUE (target_type, target_key),
  INDEX idx_target_catalog_public (target_type, enabled, sort_order, target_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO target_catalog
  (target_type, target_key, name, group_name, sort_order, enabled, updated_at)
VALUES
  ('DORM', 'lanyuan', '兰苑', '传统苑区', 10, TRUE, NOW(3)),
  ('DORM', 'meiyuan', '梅苑', '传统苑区', 20, TRUE, NOW(3)),
  ('DORM', 'zhuyuan', '竹苑', '传统苑区', 30, TRUE, NOW(3)),
  ('DORM', 'juyuan', '菊苑', '传统苑区', 40, TRUE, NOW(3)),
  ('DORM', 'taoyuan', '桃苑', '传统苑区', 50, TRUE, NOW(3)),
  ('DORM', 'liyuan', '李苑', '传统苑区', 60, TRUE, NOW(3)),
  ('DORM', 'liuyuan', '柳苑', '传统苑区', 70, TRUE, NOW(3)),
  ('DORM', 'guiyuan', '桂苑', '传统苑区', 80, TRUE, NOW(3)),
  ('DORM', 'yingyuan', '樱苑', '传统苑区', 90, TRUE, NOW(3)),
  ('DORM', 'nanhe', '南荷', '南北荷片区', 100, TRUE, NOW(3)),
  ('DORM', 'beihe', '北荷', '南北荷片区', 110, TRUE, NOW(3)),
  ('DORM', 'qingjiao', '青教', '公寓区', 120, TRUE, NOW(3)),
  ('FOOD', 'xianlin-canteen-1', '仙林一食堂', '仙林校区', 10, TRUE, NOW(3)),
  ('FOOD', 'xianlin-canteen-2', '仙林二食堂', '仙林校区', 20, TRUE, NOW(3)),
  ('FOOD', 'xianlin-canteen-3', '仙林三食堂', '仙林校区', 30, TRUE, NOW(3)),
  ('FOOD', 'xianlin-outside', '仙林校外美食', '仙林校区', 40, TRUE, NOW(3)),
  ('FOOD', 'sanpailou-canteen', '三牌楼学生食堂', '三牌楼校区', 50, TRUE, NOW(3)),
  ('FOOD', 'sanpailou-outside', '三牌楼周边餐饮', '三牌楼校区', 60, TRUE, NOW(3));

CREATE TABLE admin_operation_logs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  action VARCHAR(40) NOT NULL,
  entity_type VARCHAR(24) NOT NULL,
  entity_key VARCHAR(80) NOT NULL,
  summary VARCHAR(500) NOT NULL,
  actor VARCHAR(40) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_admin_operation_logs_created (created_at, id),
  INDEX idx_admin_operation_logs_entity (entity_type, entity_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
