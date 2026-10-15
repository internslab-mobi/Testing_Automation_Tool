package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.Module;

public interface ModuleRepository extends JpaRepository<Module, Integer> {

    boolean existsByModuleNameIgnoreCase(String moduleName);
}