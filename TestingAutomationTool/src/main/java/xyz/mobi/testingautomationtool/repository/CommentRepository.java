package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
}
