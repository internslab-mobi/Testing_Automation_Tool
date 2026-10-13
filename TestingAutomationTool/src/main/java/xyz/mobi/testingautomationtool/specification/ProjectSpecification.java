package xyz.mobi.testingautomationtool.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import xyz.mobi.testingautomationtool.entity.Project;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

import java.util.ArrayList;
import java.util.List;

public final class ProjectSpecification {

    private ProjectSpecification() {
    }

    public static Specification<Project> search(
            String keyword,
            ProjectStatus status) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Exclude deleted projects
            predicates.add(
                    cb.isFalse(root.get("isDeleted"))
            );

            // Status filter
            if (status != null) {
                predicates.add(
                        cb.equal(root.get("status"), status)
                );
            }

            // Keyword search
            if (keyword != null && !keyword.isBlank()) {

                String searchLower = keyword.trim().toLowerCase();
                String pattern = "%" + searchLower + "%";

                List<Predicate> orPredicates = new ArrayList<>();

                // Search by project fields
                orPredicates.add(
                        cb.like(
                                cb.lower(root.get("projectName")),
                                pattern
                        )
                );

                orPredicates.add(
                        cb.like(
                                cb.lower(root.get("description")),
                                pattern
                        )
                );

                orPredicates.add(
                        cb.like(
                                cb.lower(root.get("region")),
                                pattern
                        )
                );

                // Search by status name
                List<ProjectStatus> matchingStatuses = new ArrayList<>();

                for (ProjectStatus ps : ProjectStatus.values()) {

                    String statusName = ps.name().toLowerCase();
                    String statusWithSpace =
                            statusName.replace('_', ' ');

                    if (statusName.contains(searchLower)
                            || statusWithSpace.contains(searchLower)) {

                        matchingStatuses.add(ps);
                    }
                }

                if (!matchingStatuses.isEmpty()) {
                    orPredicates.add(
                            root.get("status").in(matchingStatuses)
                    );
                }

                // Search by project ID
                try {
                    Integer id = Integer.valueOf(keyword.trim());

                    orPredicates.add(
                            cb.equal(root.get("projectId"), id)
                    );

                } catch (NumberFormatException ignored) {
                }

                predicates.add(
                        cb.or(
                                orPredicates.toArray(new Predicate[0])
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}