package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByEmployee_UserIdOrderByCreatedAtDesc(Integer employeeId);
    List<Notification> findByAssigned_UserIdOrderByCreatedAtDesc(Integer assignedToId);

}
