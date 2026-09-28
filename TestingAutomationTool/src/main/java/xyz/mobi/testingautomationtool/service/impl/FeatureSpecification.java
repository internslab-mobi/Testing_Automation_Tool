package xyz.mobi.testingautomationtool.service.impl;

import org.springframework.data.jpa.domain.Specification;
import xyz.mobi.testingautomationtool.dto.request.getmethoddto.FeatureSearchRequest;
import xyz.mobi.testingautomationtool.entity.Feature;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class FeatureSpecification {

    public static Specification<Feature> search(
            FeatureSearchRequest request) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Keyword search
            if (request.getKeyword() != null
                    && !request.getKeyword().isBlank()) {

                String keyword =
                        "%" + request.getKeyword()
                                .trim()
                                .toLowerCase() + "%";

                Predicate keywordPredicate =
                        criteriaBuilder.or(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("featureName")
                                        ),
                                        keyword
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("sprint")
                                        ),
                                        keyword
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("version")
                                        ),
                                        keyword
                                )
                        );

                predicates.add(keywordPredicate);
            }

            // Project filter
            if (request.getProjectId() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("project")
                                        .get("projectId"),
                                request.getProjectId()
                        )
                );
            }

            // Feature name filter
            if (request.getFeatureName() != null
                    && !request.getFeatureName().isBlank()) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("featureName"),
                                request.getFeatureName()
                        )
                );
            }

            // Sprint filter
            if (request.getSprint() != null
                    && !request.getSprint().isBlank()) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("sprint"),
                                request.getSprint()
                        )
                );
            }

            // Version filter
            if (request.getVersion() != null
                    && !request.getVersion().isBlank()) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("version"),
                                request.getVersion()
                        )
                );
            }

            // Status filter
            if (request.getStatus() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"),
                                request.getStatus()
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
