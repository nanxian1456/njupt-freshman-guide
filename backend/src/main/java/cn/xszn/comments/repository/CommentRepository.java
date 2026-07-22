package cn.xszn.comments.repository;

import cn.xszn.comments.model.Comment;
import cn.xszn.comments.model.CommentStatus;
import cn.xszn.comments.model.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  Page<Comment> findByTargetTypeAndTargetKeyAndStatusOrderByCreatedAtDescIdDesc(
      TargetType targetType, String targetKey, CommentStatus status, Pageable pageable);

  Page<Comment> findByStatusOrderByCreatedAtAscIdAsc(CommentStatus status, Pageable pageable);
}
