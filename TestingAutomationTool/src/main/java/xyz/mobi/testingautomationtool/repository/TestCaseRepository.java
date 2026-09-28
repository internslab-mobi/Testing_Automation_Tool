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

import java.util.Optional;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Integer>, JpaSpecificationExecutor<TestCase> {

    boolean existsByTestcaseFormatId(String testcaseFormatId);

    Optional<TestCase> findByTestcaseIdAndIsDeletedFalse(Integer testcaseId);

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
}
