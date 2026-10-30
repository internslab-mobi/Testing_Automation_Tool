package xyz.mobi.testingautomationtool.dto.TestCaseDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseAuditLogResponse {

    private Integer logId;

    private Integer testcaseId;

    private String testcaseFormatId;

    private TestCaseStatus status;

    private String previousStatus;

    private Integer executedBy;

    private String executedByName;

    private Instant executedAt;

    private Integer bugId;

    private String bugFormatId;
}
