package xyz.mobi.testingautomationtool.repository;

import xyz.mobi.testingautomationtool.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseRepository extends JpaRepository<TestCase, Integer> {

    boolean existsByFeatureFeatureIdAndTestcaseFormatId(
            Integer featureId,
            String testcaseFormatId);


}
