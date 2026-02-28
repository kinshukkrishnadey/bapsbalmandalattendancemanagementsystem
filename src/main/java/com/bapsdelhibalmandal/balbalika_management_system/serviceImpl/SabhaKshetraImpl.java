package com.bapsdelhibalmandal.balbalika_management_system.serviceImpl;

import com.bapsdelhibalmandal.balbalika_management_system.model.SabhaKshetra;
import com.bapsdelhibalmandal.balbalika_management_system.repository.SabhaKshetraRepository;
import com.bapsdelhibalmandal.balbalika_management_system.service.SabhaKshetraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SabhaKshetraImpl implements SabhaKshetraService {

    @Autowired
    private SabhaKshetraRepository sabhaKshetraRepository;

    @Override
    public SabhaKshetra createSabhaKshetra(SabhaKshetra sabhaKshetra) {
        if (sabhaKshetra.getZone() != null && sabhaKshetra.getZone().getZoneId() != null
                && sabhaKshetraRepository.existsByKshetraNameAndZone_ZoneId(
                sabhaKshetra.getKshetraName(), sabhaKshetra.getZone().getZoneId())) {
            throw new IllegalArgumentException("Kshetra name already exists in this zone: " + sabhaKshetra.getKshetraName());
        }
        return sabhaKshetraRepository.save(sabhaKshetra);
    }

    @Override
    public List<SabhaKshetra> getAllSabhaKshetras() {
        return sabhaKshetraRepository.findAll();
    }

    @Override
    public Optional<SabhaKshetra> getSabhaKshetraById(Long id) {
        return sabhaKshetraRepository.findById(id);
    }

    @Override
    public SabhaKshetra updateSabhaKshetra(Long id, SabhaKshetra sabhaKshetra) {
        SabhaKshetra existing = sabhaKshetraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SabhaKshetra not found"));

        if (!existing.getKshetraName().equalsIgnoreCase(sabhaKshetra.getKshetraName())
                && sabhaKshetra.getZone() != null && sabhaKshetra.getZone().getZoneId() != null
                && sabhaKshetraRepository.existsByKshetraNameAndZone_ZoneId(
                sabhaKshetra.getKshetraName(), sabhaKshetra.getZone().getZoneId())) {
            throw new IllegalArgumentException("Kshetra name already exists in this zone: " + sabhaKshetra.getKshetraName());
        }

        existing.setKshetraName(sabhaKshetra.getKshetraName());
        if (sabhaKshetra.getZone() != null) {
            existing.setZone(sabhaKshetra.getZone());
        }
        return sabhaKshetraRepository.save(existing);
    }

    @Override
    public void deleteSabhaKshetra(Long id) {
        sabhaKshetraRepository.deleteById(id);
    }

    @Override
    public List<SabhaKshetra> getSabhaKshetraByZoneId(Long zoneId) {
        return sabhaKshetraRepository.findByZone_ZoneId(zoneId);
    }
}
