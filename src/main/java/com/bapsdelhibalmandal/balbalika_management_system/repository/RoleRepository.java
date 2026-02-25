package com.bapsdelhibalmandal.balbalika_management_system.repository;

import com.bapsdelhibalmandal.balbalika_management_system.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    boolean existsByRoleNameIgnoreCase(String roleName);
    Optional<Role> findByRoleName(String roleName);
}
