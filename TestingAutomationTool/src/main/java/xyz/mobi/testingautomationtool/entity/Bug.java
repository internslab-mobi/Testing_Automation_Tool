package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.mobi.testingautomationtool.audit.Auditable;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "testing_bugs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "testing_bugs_testcase_id_bug_format_id_unique",
                        columnNames = {"testcase_id", "bug_format_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bug extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bug_id")
    private Integer bugId;

    @Column(name = "bug_format_id", nullable = false, length = 255)
    private String bugFormatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "testcase_id", nullable = false)
    private TestCase testCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    @Builder.Default
    private Feature feature = null;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 255)
    @Builder.Default
    private BugSeverity severity = BugSeverity.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 255)
    @Builder.Default
    private BugPriority priority = BugPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 255)
    @Builder.Default
    private BugStatus status = BugStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bug_reoccured_id")
    private Bug bugReoccurred;

    @Column(name = "bug_occurance")
    private Integer bugOccurrence;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;
}