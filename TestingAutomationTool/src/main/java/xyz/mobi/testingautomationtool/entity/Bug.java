package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import xyz.mobi.testingautomationtool.audit.Auditable;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(
        name = "testing_bugs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "testing_bugs_feature_id_bug_format_id_unique",
                        columnNames = {"feature_id", "bug_format_id"}
                ),
                @UniqueConstraint(
                        name = "testing_bugs_unique",
                        columnNames = "bug_format_id"
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
    @JoinColumn(
            name = "testcase_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "testing_bugs_testcase_id_foreign"
            )
    )
    private TestCase testCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "feature_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "testing_bugs_feature_id_foreign"
            )
    )
    private Feature feature;

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
    @Column(name="category",nullable = false)
    @Builder.Default
    private BugCategory category = BugCategory.PRE_PRODUCTION;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 255)
    @Builder.Default
    private BugStatus status = BugStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "reported_by",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FKfll9ine04l2kyxqm2enmch6g5"
            )
    )
    private User reportedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "executed_by",
            foreignKey = @ForeignKey(
                    name = "testing_bugs_executed_by_foreign"
            )
    )
    private User executedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "assigned_to",
            foreignKey = @ForeignKey(
                    name = "testing_bugs_assigned_to_foreign"
            )
    )
    private User assignedTo;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "bug_occurance")
    private Integer bugOccurrence;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @Column(name= "rca_comments")
    private String comments;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "updated_by",
            foreignKey = @ForeignKey(
                    name = "fk_bugs_updated_by"
            )
    )
    private User updatedBy=null;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dynamic_fields", columnDefinition = "JSON")
    @Builder.Default
    private Map<String, Object> dynamicFields = new java.util.HashMap<>();
}