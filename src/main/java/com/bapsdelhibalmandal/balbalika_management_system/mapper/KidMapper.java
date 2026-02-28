package com.bapsdelhibalmandal.balbalika_management_system.mapper;

import com.bapsdelhibalmandal.balbalika_management_system.DTO.KidDto;
import com.bapsdelhibalmandal.balbalika_management_system.DTO.KidUpdateDto;
import com.bapsdelhibalmandal.balbalika_management_system.model.Kid;
import com.bapsdelhibalmandal.balbalika_management_system.model.SabhaKshetra;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface KidMapper {
    KidMapper INSTANCE = Mappers.getMapper(KidMapper.class);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "sabhaKshetra", ignore = true)
    @Mapping(target = "assignedSabhaKshetra", ignore = true)
    @Mapping(target = "supervisedSabhaKshetra", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateKidFromDTO(KidUpdateDto dto, @MappingTarget Kid kid);

    @Mapping(target = "sabhaKshetraId", expression = "java(kid.getSabhaKshetra() != null ? kid.getSabhaKshetra().getKshetraId() : null)")
    @Mapping(target = "assignedSabhaKshetraId", expression = "java(kid.getAssignedSabhaKshetra() != null ? kid.getAssignedSabhaKshetra().getKshetraId() : null)")
    @Mapping(target = "supervisedSabhaKshetraIds", expression = "java(mapSupervisedKshetraIds(kid))")
    @Mapping(target = "statusId", expression = "java(kid.getStatus() != null ? kid.getStatus().getStatusId() : null)")
    @Mapping(target = "roleIds", expression = "java(kid.getRoles() != null ? kid.getRoles().stream().map(r -> r.getRoleId()).collect(java.util.stream.Collectors.toSet()) : null)")
    KidDto toDto(Kid kid);

    default Set<Long> mapSupervisedKshetraIds(Kid kid) {
        if (kid.getSupervisedSabhaKshetra() == null || kid.getSupervisedSabhaKshetra().isEmpty()) {
            return Collections.emptySet();
        }
        return kid.getSupervisedSabhaKshetra().stream()
                .map(SabhaKshetra::getKshetraId)
                .collect(Collectors.toSet());
    }
}
