package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import java.util.List;
import java.util.Optional;

public interface BugRepository extends JpaRepository<Bug, Integer>, JpaSpecificationExecutor<Bug> {

    Optional<List<Bug>> findByTestCase_TestcaseId(Integer id);

    Page<Bug> findByIsActiveTrue(Pageable pageable);

    List<Bug> findByActiveTrue();

    @Modifying
    @Query("UPDATE Bug b SET b.isDeleted=true,b.isActive=false WHERE b.testCase.testcaseId =:id")
    void softDeleteBugsByTestCaseId(@Param("id") Integer id);

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

    boolean existsByBugFormatId(String bugFormatId);

    Optional<Bug> findByBugIdAndIsDeletedFalse(Integer bugId);

    Optional<Bug> findTopByFeature_FeatureIdAndBugFormatIdStartingWithOrderByBugFormatIdDesc(
            Integer featureId,
            String prefix
    );

    Optional<Bug> findByBugFormatId(String bugFormatId);
}


