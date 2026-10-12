package xyz.mobi.summary_export_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.mobi.summary_export_service.entity.Project;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    Optional<Project> findByProjectIdAndIsDeletedFalse(Integer projectId);
}
