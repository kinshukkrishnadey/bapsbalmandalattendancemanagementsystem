package com.bapsdelhibalmandal.balbalika_management_system.serviceImpl;

import com.bapsdelhibalmandal.balbalika_management_system.model.Role;
import com.bapsdelhibalmandal.balbalika_management_system.repository.RoleRepository;
import com.bapsdelhibalmandal.balbalika_management_system.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role saveRole(Role role) {
        if (roleRepository.existsByRoleNameIgnoreCase(role.getRoleName())) {
            throw new IllegalArgumentException("Role already exists: " + role.getRoleName());
        }
        return roleRepository.save(role);
    }

    @Override
    public Optional<Role> getRoleById(Integer id) {
        return roleRepository.findById(id);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public void deleteRole(Integer id) {

    }

}
