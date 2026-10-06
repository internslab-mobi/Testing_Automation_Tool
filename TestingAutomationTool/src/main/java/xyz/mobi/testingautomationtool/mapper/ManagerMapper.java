package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.stereotype.Component;
import xyz.mobi.testingautomationtool.dto.AdminDTO.ManagerResponse;
import xyz.mobi.testingautomationtool.entity.User;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public class ManagerMapper {

    public ManagerResponse toResponse(User manager) {

        return ManagerResponse.builder()
                .userId(manager.getUserId())
                .username(manager.getUsername())
                .fullName(manager.getFullName())
                .designation(manager.getDesignation())
                .skills(manager.getSkills())
                .email(manager.getEmail())
                .role(manager.getRole().getRole())
                .build();
    }
}