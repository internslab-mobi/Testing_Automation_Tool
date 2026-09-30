package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.mobi.testingautomationtool.entity.TestCase;

public interface TestCaseRepository extends JpaRepository<TestCase, Integer> {
    boolean existsByTestcaseFormatId(String testcaseFormatId);

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
}
