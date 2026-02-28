package com.bapsdelhibalmandal.balbalika_management_system.repository;

import com.bapsdelhibalmandal.balbalika_management_system.model.SabhaKshetra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SabhaKshetraRepository extends JpaRepository<SabhaKshetra, Long> {
    Optional<SabhaKshetra> findByKshetraNameAndZone_ZoneId(String kshetraName, Long zoneId);
    boolean existsByKshetraNameAndZone_ZoneId(String kshetraName, Long zoneId);
    List<SabhaKshetra> findByZone_ZoneId(Long zoneId);
}
