package com.bapsdelhibalmandal.balbalika_management_system.serviceImpl;

import com.bapsdelhibalmandal.balbalika_management_system.model.Zone;
import com.bapsdelhibalmandal.balbalika_management_system.repository.ZoneRepository;
import com.bapsdelhibalmandal.balbalika_management_system.service.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ZoneServiceImpl implements ZoneService {

    @Autowired
    private ZoneRepository zoneRepository;

    @Override
    public Zone createZone(Zone zone) {
        if (zone.getZoneName() != null && zoneRepository.existsByZoneName(zone.getZoneName())) {
            throw new IllegalArgumentException("Zone name already exists: " + zone.getZoneName());
        }
        return zoneRepository.save(zone);
    }

    @Override
    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    @Override
    public Optional<Zone> getZoneById(Long id) {
        return zoneRepository.findById(id);
    }

    @Override
    public Zone updateZone(Long id, Zone zone) {
        Zone existing = zoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found with id: " + id));
        if (zone.getZoneName() != null && !existing.getZoneName().equalsIgnoreCase(zone.getZoneName())
                && zoneRepository.existsByZoneName(zone.getZoneName())) {
            throw new IllegalArgumentException("Zone name already exists: " + zone.getZoneName());
        }
        existing.setZoneName(zone.getZoneName());
        existing.setDescription(zone.getDescription());
        return zoneRepository.save(existing);
    }

    @Override
    public void deleteZone(Long id) {
        zoneRepository.deleteById(id);
    }
}
