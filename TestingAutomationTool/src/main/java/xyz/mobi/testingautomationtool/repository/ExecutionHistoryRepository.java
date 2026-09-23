package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.ExecutionHistory;

import java.util.List;

public interface ExecutionHistoryRepository
        extends JpaRepository<ExecutionHistory, Integer> {

    List<ExecutionHistory> findByExecutionExecutionId(Integer executionId);
}
