package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.mobi.testingautomationtool.entity.Feature;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Integer> {
    Optional<Feature> findByFeatureIdAndIsDeletedFalse(Integer featureId);
    List<Feature> findByProject_ProjectIdAndIsDeletedFalse(Integer projectId);
    boolean existsByProject_ProjectIdAndFeatureName(Integer projectId, String featureName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Feature f SET f.isActive = false WHERE f.project.projectId = :projectId")
    int deactivateFeaturesByProjectId(@Param("projectId") Integer projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Feature f WHERE f.project.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Integer projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Feature f SET f.isActive = true WHERE f.project.projectId = :projectId")
    int activateFeaturesByProjectId(@Param("projectId") Integer projectId);
}
