package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "testing_audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestingAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "testcase_id", nullable = false)
    private TestCase testCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bug_id")
    private Bug bug;

    @Enumerated(EnumType.STRING)
    @Column(name = "testcase_status", nullable = false, length = 255)
    private TestCaseStatus testcaseStatus;

    @Column(name = "executed_by", nullable = false)
    private Integer executedBy;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;
}