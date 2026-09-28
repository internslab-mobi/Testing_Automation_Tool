package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import xyz.mobi.testingautomationtool.entity.Feature;

public interface FeatureRepository extends JpaRepository<Feature, Integer>,
        JpaSpecificationExecutor<Feature> {

    Page<Feature> findByProject_ProjectId(
            Integer projectId,
            Pageable pageable
    );
}
