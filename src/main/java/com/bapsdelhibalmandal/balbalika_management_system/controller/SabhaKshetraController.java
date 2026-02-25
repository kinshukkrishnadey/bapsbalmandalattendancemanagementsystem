package com.bapsdelhibalmandal.balbalika_management_system.controller;


import com.bapsdelhibalmandal.balbalika_management_system.model.SabhaKshetra;
import com.bapsdelhibalmandal.balbalika_management_system.service.SabhaKshetraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sabhakshetras")
public class SabhaKshetraController {

    @Autowired
    private SabhaKshetraService sabhaKshetraService;

    @PostMapping
    public ResponseEntity<SabhaKshetra> addSabhaKshetra(@RequestBody SabhaKshetra sabhaKshetra) {
        return ResponseEntity.ok(sabhaKshetraService.createSabhaKshetra(sabhaKshetra));
    }

    @GetMapping
    public ResponseEntity<List<SabhaKshetra>> getAllSabhaKshetras() {
        return ResponseEntity.ok(sabhaKshetraService.getAllSabhaKshetras());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SabhaKshetra> getSabhaKshetraById(@PathVariable Integer id) {
        return sabhaKshetraService.getSabhaKshetraById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SabhaKshetra> updateSabhaKshetra(@PathVariable Integer id, @RequestBody SabhaKshetra sabhaKshetra) {
        return ResponseEntity.ok(sabhaKshetraService.updateSabhaKshetra(id, sabhaKshetra));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSabhaKshetra(@PathVariable Integer id) {
        sabhaKshetraService.deleteSabhaKshetra(id);
        return ResponseEntity.noContent().build();
    }

}
