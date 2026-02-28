package com.bapsdelhibalmandal.balbalika_management_system.controller;

import com.bapsdelhibalmandal.balbalika_management_system.model.SabhaKshetra;
import com.bapsdelhibalmandal.balbalika_management_system.service.SabhaKshetraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sabhakshetras")
public class SabhaKshetraController {

    @Autowired
    private SabhaKshetraService sabhaKshetraService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CREATE_SABHAKSHETRA')")
    public ResponseEntity<SabhaKshetra> addSabhaKshetra(@RequestBody SabhaKshetra sabhaKshetra) {
        return ResponseEntity.ok(sabhaKshetraService.createSabhaKshetra(sabhaKshetra));
    }

    @GetMapping
    public ResponseEntity<List<SabhaKshetra>> getAllSabhaKshetras() {
        return ResponseEntity.ok(sabhaKshetraService.getAllSabhaKshetras());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SabhaKshetra> getSabhaKshetraById(@PathVariable Long id) {
        return sabhaKshetraService.getSabhaKshetraById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<SabhaKshetra>> getSabhaKshetraByZone(@PathVariable Long zoneId) {
        return ResponseEntity.ok(sabhaKshetraService.getSabhaKshetraByZoneId(zoneId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'CREATE_SABHAKSHETRA')")
    public ResponseEntity<SabhaKshetra> updateSabhaKshetra(@PathVariable Long id, @RequestBody SabhaKshetra sabhaKshetra) {
        return ResponseEntity.ok(sabhaKshetraService.updateSabhaKshetra(id, sabhaKshetra));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'CREATE_SABHAKSHETRA')")
    public ResponseEntity<Void> deleteSabhaKshetra(@PathVariable Long id) {
        sabhaKshetraService.deleteSabhaKshetra(id);
        return ResponseEntity.noContent().build();
    }

}
