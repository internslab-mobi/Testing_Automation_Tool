package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.Attachment;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {
    List<Attachment> findByBug_BugIdAndIsDeletedFalse(Integer bugId);
    Optional<Attachment> findTopByFeature_FeatureIdAndIsDeletedFalseOrderByCreatedAtDesc(Integer featureId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Attachment a SET a.isActive = false WHERE a.feature.project.projectId = :projectId OR a.testCase.feature.project.projectId = :projectId OR a.bug.feature.project.projectId = :projectId")
    int deactivateAttachmentsByProjectId(@Param("projectId") Integer projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Attachment a SET a.isDeleted = true, a.isActive = false WHERE a.feature.project.projectId = :projectId OR a.testCase.feature.project.projectId = :projectId OR a.bug.feature.project.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Integer projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Attachment a SET a.isActive = true WHERE (a.feature.project.projectId = :projectId OR a.testCase.feature.project.projectId = :projectId OR a.bug.feature.project.projectId = :projectId) AND a.isDeleted = false")
    int activateAttachmentsByProjectId(@Param("projectId") Integer projectId);

    List<Attachment> findAllByProject_ProjectIdAndIsDeletedFalseAndIsActiveTrue(
            Integer projectId
    );
    List<Attachment> findAllByBug_BugIdAndIsDeletedFalseAndIsActiveTrue(Integer bugId);

    boolean existsByProject_ProjectIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(Integer projectId, String filename);

    boolean existsByBug_BugIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(Integer bugId, String filename);
}
