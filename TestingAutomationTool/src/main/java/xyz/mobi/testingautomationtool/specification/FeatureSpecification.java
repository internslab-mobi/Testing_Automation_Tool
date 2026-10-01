package xyz.mobi.testingautomationtool.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import xyz.mobi.testingautomationtool.dto.FeatureDto.FeatureSearchRequest;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.enums.FeatureStatus;

import java.util.ArrayList;
import java.util.List;

public final class FeatureSpecification {

    private FeatureSpecification() {
    }

    public static Specification<Feature> search(FeatureSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude deleted features
            predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));

            if (request == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            // Keyword search
            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String keyword = "%" + request.getKeyword().trim().toLowerCase() + "%";
                Predicate keywordPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("featureName")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("featureVersion")), keyword)
                );
                predicates.add(keywordPredicate);
            }

            // Project filter
            if (request.getProjectId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("project").get("projectId"), request.getProjectId()));
            }

            // Feature name filter
            if (request.getFeatureName() != null && !request.getFeatureName().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("featureName")),
                        "%" + request.getFeatureName().trim().toLowerCase() + "%"
                ));
            }

            // Sprint filter
            if (request.getSprint() != null) {
                predicates.add(criteriaBuilder.equal(root.get("sprint"), request.getSprint()));
            }

            // Version filter
            if (request.getVersion() != null && !request.getVersion().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("featureVersion"), request.getVersion().trim()));
            }

            // Status filter
            if (request.getStatus() != null && !request.getStatus().isBlank()) {
                try {
                    FeatureStatus statusEnum = FeatureStatus.valueOf(request.getStatus().trim().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
