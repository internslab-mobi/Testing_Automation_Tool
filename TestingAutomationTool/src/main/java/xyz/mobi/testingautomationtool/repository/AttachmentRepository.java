package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {
}
