package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Integer> {

    boolean existsByProjectNameIgnoreCase(String projectName);
}
