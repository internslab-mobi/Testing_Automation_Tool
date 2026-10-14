package xyz.mobi.notificationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.mobi.notificationservice.entity.Notification;

/**
 * Spring Data JPA repository for Notification tracking entities.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
}

