package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.dto.response.getMethodDTO.InAppNotificationResponse;
import xyz.mobi.testingautomationtool.entity.InAppNotification;

import java.util.List;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Integer> {

    List<InAppNotificationResponse> findByEmployeeUserIdOrderByCreatedAtDesc(
            Integer userId
    );
}
