package xyz.mobi.summary_export_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "testing_test_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase {

    @Id
    @Column(name = "testcase_id")
    private Integer testcaseId;

    @Column(name = "feature_id", nullable = false)
    private Integer featureId;

    @Column(name = "testcase_format_id")
    private String testcaseFormatId;

    @Column(name = "title")
    private String title;

    @Column(name = "test_type")
    private String testType;

    @Column(name = "test_priority")
    private String testPriority;

    @Column(name = "testcase_status")
    private String testcaseStatus;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}
