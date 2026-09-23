package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import java.util.List;
import java.util.Optional;

public interface BugRepository extends JpaRepository<Bug, Integer> {

    Optional<Bug> findTopByTestCase_TestcaseIdOrderByBugIdDesc(Integer testcaseId);

    List<Bug> findByTestCase_TestcaseIdOrderByBugIdAsc(Integer testcaseId);

    List<Bug> findByTestCase_TestcaseIdAndActiveTrueOrderByBugIdAsc(Integer testcaseId);

    Page<Bug> findByTestCase_TestcaseIdAndActiveTrue(Integer testcaseId, Pageable pageable);

    List<Bug> findByFeature_FeatureIdOrderByBugIdAsc(Integer featureId);

    List<Bug> findByFeature_FeatureIdAndActiveTrueOrderByBugIdAsc(Integer featureId);

    Page<Bug> findByFeature_FeatureIdAndActiveTrue(Integer featureId, Pageable pageable);

    Optional<List<Bug>> findByTestCase_TestcaseId(Integer id);

    Page<Bug> findByActiveTrue(Pageable pageable);

    List<Bug> findByActiveTrue();

    Page<Bug> findByStatusAndActiveTrue(BugStatus status, Pageable pageable);

    Page<Bug> findByAssignedTo_UserIdAndActiveTrue(Integer userId, Pageable pageable);

    Page<Bug> findBySeverityAndActiveTrue(BugSeverity severity, Pageable pageable);

    Page<Bug> findByPriorityAndActiveTrue(BugPriority priority, Pageable pageable);

    Page<Bug> findByReportedByAndActiveTrue(User user,Pageable pageable);
}
