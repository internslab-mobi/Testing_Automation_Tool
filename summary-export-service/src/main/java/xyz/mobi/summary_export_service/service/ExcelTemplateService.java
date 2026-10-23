package xyz.mobi.summary_export_service.service;

import java.io.IOException;
import java.io.OutputStream;

public interface ExcelTemplateService {

    byte[] generateTemplate(Integer projectId, Integer featureId);

    void generateTemplate(OutputStream outputStream)
            throws IOException;
}
