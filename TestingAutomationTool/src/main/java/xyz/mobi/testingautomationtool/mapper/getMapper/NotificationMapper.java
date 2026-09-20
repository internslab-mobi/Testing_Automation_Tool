package xyz.mobi.testingautomationtool.mapper.getMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.NotificationResponse;
import xyz.mobi.testingautomationtool.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "employee.userId", target = "employeeId")
    @Mapping(source = "bug.bugId", target = "bugId")
    @Mapping(source = "bug.bugFormatId", target = "bugFormatId")
    NotificationResponse toResponse(Notification notification);
}
