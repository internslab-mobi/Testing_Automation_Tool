package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;
import org.springframework.data.jpa.repository.Modifying;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Integer>, JpaSpecificationExecutor<TestCase> {

    boolean existsByTestcaseFormatId(String testcaseFormatId);

    Optional<TestCase> findByTestcaseIdAndIsDeletedFalse(Integer testcaseId);
    boolean existsByFeature_FeatureIdAndTestcaseFormatId(
            Integer featureId,
            String testcaseFormatId);



    Page<TestCase> findByFeature_FeatureIdAndActiveTrue(Integer featureId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TestCase t SET t.isActive = false WHERE t.feature.project.projectId = :projectId")
    int deactivateTestCasesByProjectId(@Param("projectId") Integer projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TestCase t WHERE t.feature.project.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Integer projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TestCase t SET t.isActive = true WHERE t.feature.project.projectId = :projectId")
    int activateTestCasesByProjectId(@Param("projectId") Integer projectId);

    List<TestCase> findByFeature_FeatureIdAndIsDeletedFalse(Integer featureId);

    default List<TestCase> findByFeatureFeatureIdAndIsDeletedFalse(Integer featureId) {
        return findByFeature_FeatureIdAndIsDeletedFalse(featureId);
    }

    Page<TestCase> findByFeature_FeatureId(Integer featureId, Pageable pageable);

    @Query("SELECT tc FROM TestCase tc WHERE tc.isDeleted = false " +
           "AND (:featureId IS NULL OR tc.feature.featureId = :featureId) " +
           "AND (:status IS NULL OR tc.testcaseStatus = :status) " +
           "AND (:type IS NULL OR tc.testType = :type) " +
           "AND (:priority IS NULL OR tc.testPriority = :priority)")
    Page<TestCase> findByFeatureIdWithFilters(
            @Param("featureId") Integer featureId,
            @Param("status") TestCaseStatus status,
            @Param("type") TestType type,
            @Param("priority") TestPriority priority,
            Pageable pageable);

    boolean existsByFeatureFeatureIdAndTestcaseFormatId(Integer featureId, String testcaseFormatId);

    List<TestCase> findByFeature_Project_ProjectIdAndIsDeletedFalse(Integer projectId);

    List<TestCase> findByIsDeletedFalse();
}
