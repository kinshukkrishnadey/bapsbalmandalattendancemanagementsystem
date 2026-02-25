package com.bapsdelhibalmandal.balbalika_management_system.controller;


import com.bapsdelhibalmandal.balbalika_management_system.model.Role;
import com.bapsdelhibalmandal.balbalika_management_system.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    // ➕ Create new role
    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        return ResponseEntity.ok(roleService.saveRole(role));
    }

    // 📖 Get all roles
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    // 🔍 Get role by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoleById(@PathVariable Integer id) {
        Optional<Role> role = roleService.getRoleById(id);
        return role.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ❌ Delete role by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

}
