package xyz.mobi.summary_export_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.mobi.summary_export_service.entity.Bug;

import java.util.Collection;
import java.util.List;

@Repository
public interface BugRepository extends JpaRepository<Bug, Integer> {

    @Query("SELECT b FROM Bug b WHERE b.feature.featureId IN :featureIds AND b.isDeleted = false")
    List<Bug> findByFeatureIdInAndIsDeletedFalse(@Param("featureIds") Collection<Integer> featureIds);

    @Query("SELECT b FROM Bug b WHERE b.feature.featureId = :featureId AND b.isDeleted = false")
    List<Bug> findByFeatureIdAndIsDeletedFalse(@Param("featureId") Integer featureId);

    List<Bug> findByFeatureFeatureIdAndIsDeletedFalse(Integer featureId);
}
