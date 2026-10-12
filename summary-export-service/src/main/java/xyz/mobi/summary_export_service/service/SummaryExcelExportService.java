package xyz.mobi.summary_export_service.service;

public interface SummaryExcelExportService {

    /**
     * Generates a comprehensive multi-sheet QA Summary Report for an entire project.
     *
     * @param projectId ID of the project to export
     * @return Generated .xlsx byte array
     */
    byte[] exportProjectSummary(Integer projectId);

    /**
     * Generates a comprehensive multi-sheet QA Summary Report for a single feature.
     *
     * @param featureId ID of the feature to export
     * @return Generated .xlsx byte array
     */
    byte[] exportFeatureSummary(Integer featureId);
}
