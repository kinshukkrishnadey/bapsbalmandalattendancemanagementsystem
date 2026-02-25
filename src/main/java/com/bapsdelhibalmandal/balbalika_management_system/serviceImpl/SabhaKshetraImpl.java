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
        if (sabhaKshetraRepository.existsByKshtra(sabhaKshetra.getKshtra())) {
            throw new IllegalArgumentException("Kshetra name already exists: " + sabhaKshetra.getKshtra());
        }
        return sabhaKshetraRepository.save(sabhaKshetra);
    }

    @Override
    public List<SabhaKshetra> getAllSabhaKshetras() {
        return sabhaKshetraRepository.findAll();
    }

    @Override
    public Optional<SabhaKshetra> getSabhaKshetraById(Integer id) {
        return sabhaKshetraRepository.findById(id);
    }

    @Override
    public SabhaKshetra updateSabhaKshetra(Integer id, SabhaKshetra sabhaKshetra) {
        SabhaKshetra existing = sabhaKshetraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SabhaKshetra not found"));

        // Only check uniqueness if name is changing
        if (!existing.getKshtra().equalsIgnoreCase(sabhaKshetra.getKshtra())
                && sabhaKshetraRepository.existsByKshtra(sabhaKshetra.getKshtra())) {
            throw new IllegalArgumentException("Kshetra name already exists: " + sabhaKshetra.getKshtra());
        }

        existing.setKshtra(sabhaKshetra.getKshtra());
        return sabhaKshetraRepository.save(existing);
    }

    @Override
    public void deleteSabhaKshetra(Integer id) {
        sabhaKshetraRepository.deleteById(id);

    }
}
