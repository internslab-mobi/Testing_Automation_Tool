package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.Bug;

import java.util.List;
import java.util.Optional;

@Repository
public interface BugRepository extends JpaRepository<Bug, Integer>, JpaSpecificationExecutor<Bug> {

    @EntityGraph(attributePaths = {"testCase", "feature", "reportedBy", "assignedTo", "executedBy", "updatedBy"})
    List<Bug> findByTestCase_TestcaseId(Integer id);


    @EntityGraph(attributePaths = {"testCase", "feature", "reportedBy", "assignedTo", "executedBy", "updatedBy"})
    Page<Bug> findByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"testCase", "feature", "reportedBy", "assignedTo", "executedBy", "updatedBy"})
    Page<Bug> findByAssignedTo_UserIdAndIsActiveTrue(Integer userId, Pageable pageable);

    @EntityGraph(attributePaths = {"testCase", "feature", "reportedBy", "assignedTo", "executedBy", "updatedBy"})
    Page<Bug> findByAssignedTo_UserIdAndFeature_FeatureIdAndIsActiveTrue(Integer userId, Integer featureId, Pageable pageable);

    @EntityGraph(attributePaths = {"testCase", "feature", "reportedBy", "assignedTo", "executedBy", "updatedBy"})
    Page<Bug> findByFeature_FeatureIdAndIsDeletedFalse(Integer featureId, Pageable pageable);

    @Modifying
    @Query("UPDATE Bug b SET b.isDeleted=true, b.isActive=false WHERE b.testCase.testcaseId =:id")
    int softDeleteBugsByTestCaseId(@Param("id") Integer id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                UPDATE Bug b
                SET b.isActive = false
                WHERE b.testCase.testcaseId = :testCaseId
            """)
    int deactivateBugs(@Param("testCaseId") Integer testCaseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                UPDATE Bug b
                SET b.isActive = true
                WHERE b.testCase.testcaseId = :testCaseId
                  AND b.isDeleted = false
            """)
    int activateNonDeletedBugs(@Param("testCaseId") Integer testCaseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Bug b SET b.isActive = false WHERE b.feature.project.projectId = :projectId")
    int deactivateBugsByProjectId(@Param("projectId") Integer projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Bug b SET b.isActive = true WHERE b.feature.project.projectId = :projectId AND b.isDeleted = false")
    int activateBugsByProjectId(@Param("projectId") Integer projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Bug b SET b.isDeleted = true, b.isActive = false WHERE b.feature.project.projectId = :projectId")
    int deleteByProjectId(@Param("projectId") Integer projectId);


    Optional<Bug> findTopByFeature_FeatureIdAndBugFormatIdStartingWithOrderByBugFormatIdDesc(
            Integer featureId,
            String prefix
    );


    @EntityGraph(attributePaths = {"testCase", "feature", "reportedBy", "assignedTo", "executedBy", "updatedBy"})
    List<Bug> findByFeature_FeatureIdAndIsDeletedFalse(Integer featureId);


    @EntityGraph(attributePaths = {"testCase", "feature", "reportedBy", "assignedTo", "executedBy", "updatedBy"})
    List<Bug> findByFeature_Project_ProjectIdAndIsDeletedFalse(Integer projectId);

    @EntityGraph(attributePaths = {"testCase", "feature", "reportedBy", "assignedTo", "executedBy", "updatedBy"})
    List<Bug> findByIsDeletedFalse();

}
