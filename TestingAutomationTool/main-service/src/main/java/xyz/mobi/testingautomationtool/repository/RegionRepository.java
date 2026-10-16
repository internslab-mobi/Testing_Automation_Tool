package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.Region;

public interface RegionRepository extends JpaRepository<Region, Integer> {

    boolean existsByRegionNameIgnoreCase(String regionName);

}
