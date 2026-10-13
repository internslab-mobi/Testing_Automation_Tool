package xyz.mobi.summary_export_service.service;

public interface ExcelTemplateService {

    byte[] generateTemplate(Integer projectId, Integer featureId);
}
