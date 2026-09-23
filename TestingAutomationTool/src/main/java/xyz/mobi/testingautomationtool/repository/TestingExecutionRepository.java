package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.TestingExecution;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestingExecutionRepository
        extends JpaRepository<TestingExecution, Integer> {

    Optional<TestingExecution> findByTestCaseTestcaseId(Integer testcaseId);

    List<TestingExecution> findByTestCaseTestcaseIdIn(List<Integer> testcaseIds);
}