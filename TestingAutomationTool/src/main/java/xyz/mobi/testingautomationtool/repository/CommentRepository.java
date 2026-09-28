package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.mobi.testingautomationtool.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByBug_BugIdOrderByCreatedAtAsc(Integer bugId);

    Page<Comment> findByBug_BugIdOrderByCreatedAtAsc(Integer bugId, Pageable pageable);

    long countByBug_BugId(Integer bugId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Comment c WHERE c.bug.feature.project.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Integer projectId);
}
