package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.Bug;

import java.util.List;
import java.util.Optional;

public interface BugRepository extends JpaRepository<Bug, Integer> {

    Optional<Bug> findTopByTestCase_TestcaseIdOrderByBugIdDesc(
            Integer testcaseId);

    List<Bug> findByTestCase_TestcaseIdOrderByBugIdAsc(
            Integer testcaseId);

    Optional<Bug> findByBugReoccurred(Bug bug);

    List<Bug> findByFeature_FeatureIdOrderByBugIdAsc(
            Integer featureId);
}
