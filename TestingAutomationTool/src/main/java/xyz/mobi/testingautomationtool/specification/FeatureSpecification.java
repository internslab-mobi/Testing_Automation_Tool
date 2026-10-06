package xyz.mobi.testingautomationtool.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeatureSearchRequest;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.entity.User;

import java.util.ArrayList;
import java.util.List;

public final class FeatureSpecification {

    private FeatureSpecification() {
    }

    public static Specification<Feature> search(
            FeatureSearchRequest request) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude deleted features
            predicates.add(
                    cb.isFalse(root.get("isDeleted"))
            );

            if (request == null) {
                return cb.and(
                        predicates.toArray(new Predicate[0])
                );
            }

            // Keyword search across name, description and version
            if (request.getKeyword() != null
                    && !request.getKeyword().isBlank()) {

                String pattern = "%"
                        + request.getKeyword().trim().toLowerCase()
                        + "%";

                predicates.add(
                        cb.or(
                                cb.like(
                                        cb.lower(root.get("featureName")),
                                        pattern
                                ),
                                cb.like(
                                        cb.lower(root.get("description")),
                                        pattern
                                ),
                                cb.like(
                                        cb.lower(root.get("featureVersion")),
                                        pattern
                                )
                        )
                );
            }

            // Project filter
            if (request.getProjectId() != null) {
                predicates.add(
                        cb.equal(
                                root.get("project").get("projectId"),
                                request.getProjectId()
                        )
                );
            }

            // Feature name filter
            if (request.getFeatureName() != null
                    && !request.getFeatureName().isBlank()) {

                predicates.add(
                        cb.like(
                                cb.lower(root.get("featureName")),
                                "%" + request.getFeatureName()
                                        .trim().toLowerCase() + "%"
                        )
                );
            }

            // Description filter
            if (request.getDescription() != null
                    && !request.getDescription().isBlank()) {

                predicates.add(
                        cb.like(
                                cb.lower(root.get("description")),
                                "%" + request.getDescription()
                                        .trim().toLowerCase() + "%"
                        )
                );
            }

            // Status filter
            if (request.getStatus() != null) {
                predicates.add(
                        cb.equal(
                                root.get("status"),
                                request.getStatus()
                        )
                );
            }

            // Sprint filter
            if (request.getSprint() != null) {
                predicates.add(
                        cb.equal(
                                root.get("sprint"),
                                request.getSprint()
                        )
                );
            }

            // Version filter
            if (request.getFeatureVersion() != null
                    && !request.getFeatureVersion().isBlank()) {

                predicates.add(
                        cb.equal(
                                cb.lower(root.get("featureVersion")),
                                request.getFeatureVersion()
                                        .trim().toLowerCase()
                        )
                );
            }

            // Created-by filter
            if (request.getCreatedBy() != null
                    && !request.getCreatedBy().isBlank()) {

                Join<Feature, User> createdByJoin =
                        root.join("createdBy", JoinType.LEFT);

                predicates.add(
                        cb.like(
                                cb.lower(createdByJoin.get("username")),
                                "%" + request.getCreatedBy()
                                        .trim().toLowerCase() + "%"
                        )
                );
            }

            // Updated-by filter
            if (request.getUpdatedBy() != null
                    && !request.getUpdatedBy().isBlank()) {

                Join<Feature, User> updatedByJoin =
                        root.join("updatedBy", JoinType.LEFT);

                predicates.add(
                        cb.like(
                                cb.lower(updatedByJoin.get("username")),
                                "%" + request.getUpdatedBy()
                                        .trim().toLowerCase() + "%"
                        )
                );
            }

            // Comments filter
            if (request.getComments() != null
                    && !request.getComments().isBlank()) {

                predicates.add(
                        cb.like(
                                cb.lower(root.get("comments")),
                                "%" + request.getComments()
                                        .trim().toLowerCase() + "%"
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
