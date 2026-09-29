package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.Attachment;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {
    List<Attachment> findByBug_BugIdAndIsDeletedFalse(Integer bugId);
    List<Attachment> findByTestCase_TestcaseIdAndIsDeletedFalse(Integer testcaseId);
}
