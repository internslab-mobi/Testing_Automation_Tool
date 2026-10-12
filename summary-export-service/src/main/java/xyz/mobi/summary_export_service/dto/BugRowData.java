package xyz.mobi.summary_export_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugRowData {
    private String featureName;
    private String bugFormatId;
    private String title;
    private String description;
    private String severity;
    private String priority;
    private String status;
    private String category;
    private Integer bugOccurrence;
    private String assignedTo;
    private String rcaComments;
}
