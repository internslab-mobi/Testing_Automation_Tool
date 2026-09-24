package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.BugHistory;

public interface BugHistoryRepository extends JpaRepository<BugHistory, Integer> {
}
