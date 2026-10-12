package xyz.mobi.testingautomationtool.client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.mobi.testingautomationtool.exception.ExcelProcessingException;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Internal HTTP Client using standard java.net.http.HttpClient (JDK 21)
 * to communicate with the isolated summary-export-service microservice.
 */
@Slf4j
@Component
public class SummaryExportClient {

    private final String baseUrl;
    private final HttpClient httpClient;

    public SummaryExportClient(
            @Value("${app.services.summary-export.url:http://localhost:8092}") String baseUrl
    ) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public byte[] exportProjectSummary(Integer projectId) {
        String targetUrl = baseUrl + "/export/summary/project/" + projectId;
        log.info("Internally calling summary-export-service for Project ID: {} -> {}", projectId, targetUrl);
        return executeGetRequest(targetUrl, "Project summary export for Project ID: " + projectId);
    }

    public byte[] exportFeatureSummary(Integer featureId) {
        String targetUrl = baseUrl + "/export/summary/feature/" + featureId;
        log.info("Internally calling summary-export-service for Feature ID: {} -> {}", featureId, targetUrl);
        return executeGetRequest(targetUrl, "Feature summary export for Feature ID: " + featureId);
    }

    public byte[] downloadTestCaseTemplate(Integer projectId, Integer featureId) {
        String targetUrl = baseUrl + "/export/template/" + projectId + "/" + featureId;
        log.info("Internally calling summary-export-service for Template (Project ID: {}, Feature ID: {}) -> {}", projectId, featureId, targetUrl);
        return executeGetRequest(targetUrl, "Test case template for Feature ID: " + featureId);
    }

    private byte[] executeGetRequest(String url, String operationDescription) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/octet-stream")
                .GET();

        // Forward caller's Authorization token if present
        String authHeader = getCurrentAuthorizationHeader();
        if (authHeader != null && !authHeader.isBlank()) {
            requestBuilder.header("Authorization", authHeader);
        }

        try {
            HttpResponse<byte[]> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                return response.body();
            } else if (statusCode == 404) {
                log.warn("summary-export-service returned 404 Not Found for {}: {}", url, new String(response.body()));
                throw new ResourceNotFoundException("Resource not found in export service: " + operationDescription);
            } else {
                String errorBody = new String(response.body());
                log.error("summary-export-service failed with status {} for {}: {}", statusCode, url, errorBody);
                throw new ExcelProcessingException("Export service returned error (HTTP " + statusCode + "): " + errorBody);
            }
        } catch (IOException e) {
            log.error("I/O error communicating with summary-export-service at {}: {}", url, e.getMessage());
            throw new ExcelProcessingException("Failed to communicate with summary export microservice: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Export request interrupted for {}: {}", url, e.getMessage());
            throw new ExcelProcessingException("Export request was interrupted: " + e.getMessage(), e);
        }
    }

    private String getCurrentAuthorizationHeader() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("Authorization");
            }
        } catch (Exception e) {
            log.debug("Unable to extract current Authorization header: {}", e.getMessage());
        }
        return null;
    }
}
