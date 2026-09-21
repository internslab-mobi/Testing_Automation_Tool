package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;

import java.util.List;
import java.util.Optional;

public interface TestingExecutionRepository
        extends JpaRepository<TestingExecution, Integer> {

    Optional<TestingExecution> findByTestCase(TestCase testCase);

    Optional<TestingExecution> findByTestCaseTestcaseId(Integer testcaseId);

}
