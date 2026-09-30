package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.mobi.testingautomationtool.entity.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Attachment a SET a.isActive = false WHERE a.feature.project.projectId = :projectId OR a.testCase.feature.project.projectId = :projectId OR a.bug.feature.project.projectId = :projectId")
    int deactivateAttachmentsByProjectId(@Param("projectId") Integer projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Attachment a WHERE a.feature.project.projectId = :projectId OR a.testCase.feature.project.projectId = :projectId OR a.bug.feature.project.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Integer projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Attachment a SET a.isActive = true WHERE a.feature.project.projectId = :projectId OR a.testCase.feature.project.projectId = :projectId OR a.bug.feature.project.projectId = :projectId")
    int activateAttachmentsByProjectId(@Param("projectId") Integer projectId);
}
