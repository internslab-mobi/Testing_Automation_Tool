package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;

import java.util.List;
import java.util.Optional;

public interface TestingExecutionRepository
        extends JpaRepository<TestingExecution, Integer> {

    Optional<TestingExecution> findByTestCase(TestCase testCase);

    Optional<TestingExecution> findByTestCaseTestcaseId(Integer testcaseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TestingExecution te WHERE te.testCase.feature.project.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Integer projectId);


    List<TestingExecution> findByTestCaseTestcaseIdIn(List<Integer> testcaseIds);
}