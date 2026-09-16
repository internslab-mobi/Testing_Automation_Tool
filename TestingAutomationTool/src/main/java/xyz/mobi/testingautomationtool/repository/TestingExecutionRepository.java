package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.TestingExecution;

import java.util.List;

public interface TestingExecutionRepository
        extends JpaRepository<TestingExecution, Integer> {

    List<TestingExecution> findByTestCaseTestcaseId(Integer testcaseId);
}
