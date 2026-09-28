package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.Feature;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Integer> {
    Optional<Feature> findByFeatureIdAndIsDeletedFalse(Integer featureId);
    List<Feature> findByProject_ProjectIdAndIsDeletedFalse(Integer projectId);
    boolean existsByProject_ProjectIdAndFeatureName(Integer projectId, String featureName);
}