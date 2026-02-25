package com.bapsdelhibalmandal.balbalika_management_system.Seed;

import com.bapsdelhibalmandal.balbalika_management_system.enums.Right;
import com.bapsdelhibalmandal.balbalika_management_system.model.Role;
import com.bapsdelhibalmandal.balbalika_management_system.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RoleSeeder {

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    public void seedRoles() {
        if (roleRepository.findByRoleName("SUPERADMIN").isEmpty()) {
            Role superAdmin = Role.builder()
                    .roleName("SUPERADMIN")
                    .rights(Set.of(
                            Right.CREATE_ROLE,
                            Right.CREATE_SABHAKSHETRA,
                            Right.ADD_KID,
                            Right.UPDATE_KID,
                            Right.MAP_STATUS,
                            Right.VIEW_KIDS,
                            Right.VIEW_ATTENDANCE
                    ))
                    .build();

            roleRepository.save(superAdmin);
        }

        // add more roles like ADMIN, SANCHALAK, etc. if needed
    }
}
