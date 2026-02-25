package com.bapsdelhibalmandal.balbalika_management_system.mapper;


import com.bapsdelhibalmandal.balbalika_management_system.DTO.KidDto;
import com.bapsdelhibalmandal.balbalika_management_system.DTO.KidUpdateDto;
import com.bapsdelhibalmandal.balbalika_management_system.model.Kid;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface KidMapper {
    KidMapper INSTANCE = Mappers.getMapper(KidMapper.class);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateKidFromDTO(KidUpdateDto dto, @MappingTarget Kid kid);

    // Optional: Entity → DTO for response mapping
    KidDto toDto(Kid kid);

}
