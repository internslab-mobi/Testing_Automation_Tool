package xyz.mobi.testingautomationtool.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Integer>, JpaSpecificationExecutor<Feature> {

    Optional<Feature> findByFeatureIdAndIsDeletedFalse(Integer featureId);

    Optional<Feature> findByFeatureIdAndIsActiveTrueAndIsDeletedFalse(Integer featureId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Feature f WHERE f.featureId = :featureId AND f.isDeleted = false")
    Optional<Feature> findByFeatureIdForUpdate(@Param("featureId") Integer featureId);

    List<Feature> findByProject_ProjectIdAndIsDeletedFalse(Integer projectId);

    List<Feature> findByIsDeletedFalse();

    boolean existsByProject_ProjectIdAndFeatureName(Integer projectId, String featureName);

    @Query("SELECT f FROM Feature f WHERE f.featureId = :featureId AND f.project.projectId = :projectId AND f.isDeleted = false AND f.project.isActive = true")
    Optional<Feature> findActiveFeatureByProject(@Param("projectId") Integer projectId, @Param("featureId") Integer featureId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Feature f SET f.isActive = false WHERE f.project.projectId = :projectId")
    void deactivateFeaturesByProjectId(@Param("projectId") Integer projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Feature f SET f.isDeleted = true, f.isActive = false WHERE f.project.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Integer projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Feature f SET f.isActive = true WHERE f.project.projectId = :projectId AND f.isDeleted = false")
    void activateFeaturesByProjectId(@Param("projectId") Integer projectId);

    Optional<Feature> findByFeatureIdAndIsDeletedFalseAndProjectStatus(
            Integer featureId,
            ProjectStatus status
    );

    List<Feature> findByProject_ProjectIdAndIsDeletedFalseAndIsActiveTrueAndProject_Status(Integer projectId, ProjectStatus projectStatus);

}