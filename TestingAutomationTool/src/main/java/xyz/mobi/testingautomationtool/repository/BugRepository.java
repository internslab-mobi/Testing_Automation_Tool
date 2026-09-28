package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.Bug;

import java.util.List;

public interface BugRepository extends JpaRepository<Bug, Integer> {

    List<Bug> findByFeatureFeatureIdAndIsDeletedFalse(
            Integer featureId);
}
