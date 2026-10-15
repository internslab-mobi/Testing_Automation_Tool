package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.SubModule;

public interface SubModuleRepository
        extends JpaRepository<SubModule, Integer> {

    boolean existsByModule_ModuleIdAndSubModuleNameIgnoreCase(
            Integer moduleId,
            String subModuleName
    );
}