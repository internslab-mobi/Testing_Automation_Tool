package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.Bug;

import java.util.Optional;

@Repository
public interface BugRepository extends JpaRepository<Bug, Integer> {
    boolean existsByBugFormatId(String bugFormatId);
    Optional<Bug> findByBugIdAndIsDeletedFalse(Integer bugId);
    Optional<Bug> findByBugFormatId(String bugFormatId);
}
