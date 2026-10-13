package xyz.mobi.summary_export_service.service;

public interface SummaryExcelExportService {
    byte[] exportProjectSummary(Integer projectId);

    byte[] exportFeatureSummary(Integer featureId);
}
