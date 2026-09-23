package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "testing_test_cases",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "testing_test_cases_feature_id_testcase_format_id_unique",
                        columnNames = {"feature_id", "testcase_format_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "testcase_id")
    private Integer testcaseId;

    @Column(name = "feature_id", nullable = false)
    private Integer featureId;

    @Column(name = "testcase_format_id", nullable = false, length = 255)
    private String testcaseFormatId;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false, length = 255)
    private TestType testType;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_priority", nullable = false, length = 255)
    private TestPriority testPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "testcase_status", nullable = false, length = 255)
    private TestCaseStatus testcaseStatus;

    @Column(name = "is_active", nullable = false)
    private boolean activeStatus;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
