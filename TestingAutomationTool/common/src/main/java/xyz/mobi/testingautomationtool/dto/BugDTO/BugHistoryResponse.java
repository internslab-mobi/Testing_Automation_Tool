package xyz.mobi.testingautomationtool.dto.BugDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugHistoryResponse {

    private Integer bugLogId;

    private Integer bugId;

    private String bugFormatId;

    private BugStatus bugStatus;

    private String previousStatus;

    private Integer executedBy;

    private String executedByName;

    private Integer assignedTo;

    private String assignedToName;

    private Instant resolvedAt;

    private Instant createdAt;
}
