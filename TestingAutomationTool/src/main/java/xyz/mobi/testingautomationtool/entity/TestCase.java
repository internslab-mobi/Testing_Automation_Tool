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
                        name = "testing_test_cases_unique",
                        columnNames = "testcase_format_id"
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

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false, length = 20)
    private TestType testType;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_priority", nullable = false, length = 20)
    private TestPriority testPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "testcase_status", nullable = false, length = 20)
    private TestCaseStatus testcaseStatus;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(
            name = "testcase_format_id",
            nullable = false,
            unique = true,
            length = 20
    )
    private String testcaseFormatId;
}