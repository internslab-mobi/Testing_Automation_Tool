package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.mobi.testingautomationtool.audit.Auditable;
import xyz.mobi.testingautomationtool.enums.AttachmentType;

@Entity
@Table(name = "testing_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Integer attachmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", nullable = false, length = 50)
    private AttachmentType attachmentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "feature_id",
            foreignKey = @ForeignKey(name = "testing_attachments_feature_id_foreign")
    )
    private Feature feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "bug_id",
            foreignKey = @ForeignKey(name = "testing_attachments_bug_id_foreign")
    )
    private Bug bug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "testcase_id",
            foreignKey = @ForeignKey(name = "testing_attachments_testcase_id_foreign")
    )
    private TestCase testCase;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Lob
    @Column(name = "file_data", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] fileData;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "uploaded_by",
            nullable = false,
            foreignKey = @ForeignKey(name = "testing_attachments_uploaded_by_foreign")
    )
    private User uploadedBy;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "updated_by",
            foreignKey = @ForeignKey(name = "fk_attachments_updated_by")
    )
    private User updatedBy;
}
