package xyz.mobi.summary_export_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.mobi.summary_export_service.entity.TestCase;

import java.util.Collection;
import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Integer> {
    List<TestCase> findByFeatureIdInAndIsDeletedFalse(Collection<Integer> featureIds);
    List<TestCase> findByFeatureIdAndIsDeletedFalse(Integer featureId);
}
