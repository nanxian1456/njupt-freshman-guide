package cn.xszn.comments.repository;

import cn.xszn.comments.model.AdminOperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminOperationLogRepository extends JpaRepository<AdminOperationLog, Long> {
  Page<AdminOperationLog> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);
}
