package xyz.mobi.testingautomationtool.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import xyz.mobi.testingautomationtool.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Integer> {
    boolean existsByTestcaseFormatId(String testcaseFormatId);

    boolean existsByFeature_FeatureIdAndTestcaseFormatId(
            Integer featureId,
            String testcaseFormatId);

//    Page<TestCase> findByFeature_FeatureId(Integer featureId, Pageable pageable);

    Page<TestCase> findByFeature_FeatureIdAndActiveTrue(Integer featureId, Pageable pageable);
}
