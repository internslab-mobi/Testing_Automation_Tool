package xyz.mobi.notificationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Standard pagination response DTO.
 * Avoids exposing internal framework classes like PageImpl to API consumers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response wrapper")
public class PaginatedResponse<T> {

    @Schema(description = "List of items in current page")
    private List<T> items;

    @Schema(description = "Current page number (0-based)", example = "0")
    private int page;

    @Schema(description = "Configured page size", example = "20")
    private int size;

    @Schema(description = "Total number of elements across all pages", example = "100")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;

    @Schema(description = "Indicates if this is the last page", example = "false")
    private boolean last;

    public static <T> PaginatedResponse<T> fromPage(Page<T> springPage) {
        return PaginatedResponse.<T>builder()
                .items(springPage.getContent())
                .page(springPage.getNumber())
                .size(springPage.getSize())
                .totalElements(springPage.getTotalElements())
                .totalPages(springPage.getTotalPages())
                .last(springPage.isLast())
                .build();
    }
}
