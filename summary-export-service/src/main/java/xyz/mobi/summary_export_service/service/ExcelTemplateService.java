package xyz.mobi.summary_export_service.service;

public interface ExcelTemplateService {

    /**
     * Generates the QA Testcase and Bug Details template/export for a feature within a project.
     *
     * @param projectId Project ID
     * @param featureId Feature ID
     * @return Generated .xlsx byte array
     */
    byte[] generateTemplate(Integer projectId, Integer featureId);
}
