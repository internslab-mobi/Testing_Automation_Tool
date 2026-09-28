package xyz.mobi.testingautomationtool.mapper.getMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.NotificationRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.NotificationResponse;
import xyz.mobi.testingautomationtool.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "employeeId", source = "employee.userId")
    @Mapping(target = "assignedId", source = "assigned.userId")
    @Mapping(target = "bugId", source = "bug.bugId")
    NotificationResponse toResponse(Notification notification);

}
