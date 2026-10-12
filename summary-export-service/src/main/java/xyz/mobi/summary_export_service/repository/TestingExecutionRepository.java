package xyz.mobi.summary_export_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.mobi.summary_export_service.entity.TestingExecution;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestingExecutionRepository extends JpaRepository<TestingExecution, Integer> {
    List<TestingExecution> findByTestcaseIdIn(Collection<Integer> testcaseIds);
    Optional<TestingExecution> findFirstByTestcaseIdOrderByExecutionIdDesc(Integer testcaseId);
}
