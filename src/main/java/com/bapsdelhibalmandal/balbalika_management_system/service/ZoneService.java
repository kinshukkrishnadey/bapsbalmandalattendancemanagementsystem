package com.bapsdelhibalmandal.balbalika_management_system.service;

import com.bapsdelhibalmandal.balbalika_management_system.model.Zone;

import java.util.List;
import java.util.Optional;

public interface ZoneService {
    Zone createZone(Zone zone);
    List<Zone> getAllZones();
    Optional<Zone> getZoneById(Long id);
    Zone updateZone(Long id, Zone zone);
    void deleteZone(Long id);
}
