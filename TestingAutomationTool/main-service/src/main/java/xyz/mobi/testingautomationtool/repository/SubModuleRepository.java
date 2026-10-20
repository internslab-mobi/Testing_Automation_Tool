package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.SubModule;

import java.util.List;

public interface SubModuleRepository
        extends JpaRepository<SubModule, Integer> {

    List<SubModule> findByModule_ModuleId(Integer moduleId);

    boolean existsByModule_ModuleIdAndSubModuleNameIgnoreCase(
            Integer moduleId,
            String subModuleName
    );
}