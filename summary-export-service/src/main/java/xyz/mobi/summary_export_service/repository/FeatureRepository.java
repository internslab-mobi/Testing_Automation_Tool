package xyz.mobi.summary_export_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.mobi.summary_export_service.entity.Feature;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Integer> {

    List<Feature> findByProjectIdAndIsDeletedFalse(Integer projectId);

    Optional<Feature> findByFeatureIdAndIsDeletedFalse(Integer featureId);

    Optional<Feature> findByProjectIdAndFeatureIdAndIsDeletedFalse(Integer projectId, Integer featureId);

    @Query("SELECT f FROM Feature f WHERE f.projectId = :projectId AND f.featureId = :featureId AND f.isDeleted = false")
    Optional<Feature> findActiveFeatureByProject(@Param("projectId") Integer projectId, @Param("featureId") Integer featureId);
}
