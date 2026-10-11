package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.NotificationDTO.InAppNotificationResponse;
import xyz.mobi.testingautomationtool.dto.NotificationDTO.NotificationRequest;
import xyz.mobi.testingautomationtool.dto.NotificationDTO.NotificationResponse;
import xyz.mobi.testingautomationtool.entity.InAppNotification;
import xyz.mobi.testingautomationtool.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "employeeId", source = "employee.userId")
    @Mapping(target = "assignedId", source = "assigned.userId")
    @Mapping(target = "bugId", source = "bug.bugId")
    NotificationResponse toResponse(Notification notification);

    @Mapping(target = "notificationId", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "assigned", ignore = true)
    @Mapping(target = "bug", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "notificationStatus", ignore = true)
    Notification toEntity(NotificationRequest request);

    @Mapping(target = "notificationId", source = "notificationId")
    @Mapping(target = "bugId", source = "bug.bugId")
    @Mapping(target = "bugFormatId", source = "bug.bugFormatId")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "notificationStatus", expression = "java(notification.getNotificationStatus() != null ? notification.getNotificationStatus().name() : null)")
    @Mapping(target = "createdAt", source = "createdAt")
    InAppNotificationResponse toInAppResponse(InAppNotification notification);
}
