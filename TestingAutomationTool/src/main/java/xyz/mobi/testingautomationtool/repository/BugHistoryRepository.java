package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.BugHistory;

import java.util.List;

@Repository
public interface BugHistoryRepository extends JpaRepository<BugHistory, Integer> {
}
