package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.Bug;

public interface BugRepository extends JpaRepository<Bug, Integer> {
    boolean existsByBugFormatId(String bugFormatId);
}
