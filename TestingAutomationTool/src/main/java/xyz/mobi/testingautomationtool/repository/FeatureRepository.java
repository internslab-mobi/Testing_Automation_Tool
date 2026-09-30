package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.mobi.testingautomationtool.entity.Feature;

public interface FeatureRepository extends JpaRepository<Feature, Integer> {

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
