package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.mobi.testingautomationtool.entity.Feature;

import java.util.Optional;

public interface FeatureRepository extends JpaRepository<Feature, Integer>,
        JpaSpecificationExecutor<Feature> {

    Page<Feature> findByProject_ProjectId(
            Integer projectId,
            Pageable pageable
    );

    @Query("""
    SELECT f
    FROM Feature f
    WHERE f.featureId = :featureId
      AND f.project.projectId = :projectId
      AND f.project.isActive = TRUE
""")
    Optional<Feature> findActiveFeatureByProject(
            @Param("projectId") Integer projectId,
            @Param("featureId") Integer featureId);

}
