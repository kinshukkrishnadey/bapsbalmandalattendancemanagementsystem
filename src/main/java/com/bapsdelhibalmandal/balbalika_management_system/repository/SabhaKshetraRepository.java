package com.bapsdelhibalmandal.balbalika_management_system.repository;

import com.bapsdelhibalmandal.balbalika_management_system.model.SabhaKshetra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SabhaKshetraRepository extends JpaRepository<SabhaKshetra, Integer> {
    Optional<SabhaKshetra> findByKshtra(String kshtra);
    boolean existsByKshtra(String kshtra);
}
