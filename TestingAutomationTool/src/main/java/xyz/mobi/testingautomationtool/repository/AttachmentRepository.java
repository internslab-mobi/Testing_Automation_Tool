package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.Attachment;

import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {

    Optional<Attachment> findByFeatureFeatureId(Integer featureId);
}
