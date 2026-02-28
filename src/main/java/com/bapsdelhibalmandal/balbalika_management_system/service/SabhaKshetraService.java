package com.bapsdelhibalmandal.balbalika_management_system.service;

import com.bapsdelhibalmandal.balbalika_management_system.model.SabhaKshetra;

import java.util.List;
import java.util.Optional;

public interface SabhaKshetraService {
    SabhaKshetra createSabhaKshetra(SabhaKshetra sabhaKshetra);
    List<SabhaKshetra> getAllSabhaKshetras();
    Optional<SabhaKshetra> getSabhaKshetraById(Long id);
    SabhaKshetra updateSabhaKshetra(Long id, SabhaKshetra sabhaKshetra);
    void deleteSabhaKshetra(Long id);
    List<SabhaKshetra> getSabhaKshetraByZoneId(Long zoneId);
}
