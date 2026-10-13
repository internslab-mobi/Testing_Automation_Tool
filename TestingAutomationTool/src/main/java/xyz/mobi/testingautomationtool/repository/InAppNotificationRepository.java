package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.InAppNotification;

import java.util.List;

@Repository
public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Integer> {
    List<InAppNotification> findByEmployee_UserIdOrderByCreatedAtDesc(Integer userId);
}
