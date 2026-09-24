package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import xyz.mobi.testingautomationtool.audit.Auditable;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(
        name = "testing_test_cases",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "testing_test_cases_feature_id_testcase_format_id_unique",
                        columnNames = {"feature_id", "testcase_format_id"}
                ),
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
public class TestCase extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "testcase_id")
    private Integer testcaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "feature_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "testing_test_cases_feature_id_foreign"
            )
    )
    private Feature feature;

    @Column(name = "testcase_format_id", nullable = false, length = 255)
    private String testcaseFormatId;

    @Column(name = "title",  length = 300)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type",  length = 255)
    @Builder.Default
    private TestType testType = TestType.E2E;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_priority", length = 255)
    @Builder.Default
    private TestPriority testPriority = TestPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "testcase_status", length = 255)
    @Builder.Default
    private TestCaseStatus testcaseStatus = TestCaseStatus.NO_RUN;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "testing_test_cases_created_by_foreign"
            )
    )
    private User createdBy;

//    @Convert(converter = xyz.mobi.testingautomationtool.utils.JsonToMapConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dynamic_fields", columnDefinition = "JSON")
    @Builder.Default
    private Map<String, Object> dynamicFields = new HashMap<>();
}