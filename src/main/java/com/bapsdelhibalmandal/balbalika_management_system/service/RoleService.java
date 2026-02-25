package com.bapsdelhibalmandal.balbalika_management_system.service;

import com.bapsdelhibalmandal.balbalika_management_system.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    Role saveRole(Role role);
    Optional<Role> getRoleById(Integer id);
    List<Role> getAllRoles();
    void deleteRole(Integer id);
}
