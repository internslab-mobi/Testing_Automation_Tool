package xyz.mobi.testingautomationtool.repository;

import xyz.mobi.testingautomationtool.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Integer> {

    boolean existsByFeatureFeatureIdAndTestcaseFormatId(
            Integer featureId,
            String testcaseFormatId);

    List<TestCase> findByFeatureFeatureIdAndIsDeletedFalse(
            Integer featureId);
}
