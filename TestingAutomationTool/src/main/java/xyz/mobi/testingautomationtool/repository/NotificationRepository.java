package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByEmployee_UserIdOrderByCreatedAtDesc(
            Integer employeeId);
}
