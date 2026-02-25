package com.bapsdelhibalmandal.balbalika_management_system.mapper;


import com.bapsdelhibalmandal.balbalika_management_system.DTO.UserDto;
import com.bapsdelhibalmandal.balbalika_management_system.model.Role;
import com.bapsdelhibalmandal.balbalika_management_system.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "kid", target = "kid", qualifiedByName = "kidToBoolean")
    @Mapping(source = "kid.kidId", target = "kidId")
    @Mapping(source = "roles", target = "roleNames", qualifiedByName = "roleSetToNameSet")
    UserDto toDto(User user);

    @Named("kidToBoolean")
    static boolean kidToBoolean(com.bapsdelhibalmandal.balbalika_management_system.model.Kid kid) {
        return kid != null;
    }

    @Named("roleSetToNameSet")
    static Set<String> mapRoleNames(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());
    }

}
