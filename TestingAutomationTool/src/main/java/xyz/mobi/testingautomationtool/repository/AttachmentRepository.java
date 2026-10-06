package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.Attachment;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Attachment a SET a.isActive = false WHERE a.feature.project.projectId = :projectId OR a.testCase.feature.project.projectId = :projectId OR a.bug.feature.project.projectId = :projectId")
    int deactivateAttachmentsByProjectId(@Param("projectId") Integer projectId);

    List<Attachment> findAllByTestCase_TestcaseIdAndIsDeletedFalseAndIsActiveTrue(Integer testcaseId);

    boolean existsByTestCase_TestcaseIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(Integer testcaseId,String cleanFileName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Attachment a SET a.isDeleted = true, a.isActive = false WHERE a.feature.project.projectId = :projectId OR a.testCase.feature.project.projectId = :projectId OR a.bug.feature.project.projectId = :projectId")
    int deleteByProjectId(@Param("projectId") Integer projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Attachment a SET a.isActive = true WHERE (a.feature.project.projectId = :projectId OR a.testCase.feature.project.projectId = :projectId OR a.bug.feature.project.projectId = :projectId) AND a.isDeleted = false")
    int activateAttachmentsByProjectId(@Param("projectId") Integer projectId);

    List<Attachment> findAllByProject_ProjectIdAndIsDeletedFalseAndIsActiveTrue(Integer projectId);

    List<Attachment> findAllByBug_BugIdAndIsDeletedFalseAndIsActiveTrue(Integer bugId);

    List<Attachment> findAllByFeature_FeatureIdAndIsDeletedFalseAndIsActiveTrue(Integer featureId);

    List<Attachment> findAllByTestCase_TestcaseIdAndIsDeletedFalseAndIsActiveTrue(Integer testcaseId);

    boolean existsByProject_ProjectIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(Integer projectId, String filename);

    boolean existsByBug_BugIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(Integer bugId, String filename);

    boolean existsByFeature_FeatureIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(Integer featureId, String filename);

    boolean existsByTestCase_TestcaseIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(Integer testcaseId, String filename);
}
