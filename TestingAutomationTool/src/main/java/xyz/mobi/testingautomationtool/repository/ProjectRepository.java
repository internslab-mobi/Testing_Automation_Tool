package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.Project;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer>, JpaSpecificationExecutor<Project> {
    List<Project> findByIsDeletedFalse();
    Optional<Project> findByProjectIdAndIsDeletedFalse(Integer projectId);
    boolean existsByProjectNameAndRegion(String projectName, String region);

    Optional<Project> findByIdAndIsActiveTrueAndIsDeletedFalse( Integer projectId, ProjectStatus projectStatus);
}
