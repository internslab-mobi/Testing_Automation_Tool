package xyz.mobi.testingautomationtool.specification;

import org.springframework.data.jpa.domain.Specification;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public final class BugSpecification {

    private BugSpecification() {
    }

    public static Specification<Bug> search(
            String keyword,
            BugSeverity severity,
            BugPriority priority,
            BugStatus status,
            BugCategory category,
            Integer bugOccurrence,
            Boolean isActive,
            Instant resolvedFrom,
            Instant resolvedTo
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            /*
             * Always exclude soft-deleted bugs.
             */
            predicates.add(
                    cb.isFalse(root.get("isDeleted"))
            );

            /*
             * Global keyword search.
             */
            if (keyword != null && !keyword.isBlank()) {

                String search = "%" + keyword.trim().toLowerCase() + "%";

                List<Predicate> keywordPredicates = new ArrayList<>();

                keywordPredicates.add(
                        cb.like(
                                cb.lower(root.get("bugFormatId")),
                                search
                        )
                );

                keywordPredicates.add(
                        cb.like(
                                cb.lower(root.get("title")),
                                search
                        )
                );

                keywordPredicates.add(
                        cb.like(
                                cb.lower(root.get("description")),
                                search
                        )
                );

                keywordPredicates.add(
                        cb.like(
                                cb.lower(root.get("comments")),
                                search
                        )
                );

                /*
                 * Testcase ID.
                 */
                keywordPredicates.add(
                        cb.like(
                                cb.toString(
                                        root.get("testCase").get("testcaseId")
                                ),
                                search
                        )
                );

                /*
                 * Feature ID.
                 */
                keywordPredicates.add(
                        cb.like(
                                cb.toString(
                                        root.get("feature").get("featureId")
                                ),
                                search
                        )
                );

                /*
                 * User IDs.
                 */
                keywordPredicates.add(
                        cb.like(
                                cb.toString(
                                        root.get("reportedBy").get("userId")
                                ),
                                search
                        )
                );

                keywordPredicates.add(
                        cb.like(
                                cb.toString(
                                        root.get("executedBy").get("userId")
                                ),
                                search
                        )
                );

                keywordPredicates.add(
                        cb.like(
                                cb.toString(
                                        root.get("assignedTo").get("userId")
                                ),
                                search
                        )
                );

                keywordPredicates.add(
                        cb.like(
                                cb.toString(
                                        root.get("updatedBy").get("userId")
                                ),
                                search
                        )
                );

                /*
                 * Dynamic JSON fields.
                 *
                 * MySQL JSON -> text conversion.
                 */
                keywordPredicates.add(
                        cb.like(
                                cb.lower(
                                        cb.function(
                                                "JSON_UNQUOTE",
                                                String.class,
                                                root.get("dynamicFields")
                                        )
                                ),
                                search
                        )
                );

                predicates.add(
                        cb.or(
                                keywordPredicates.toArray(new Predicate[0])
                        )
                );
            }

            /*
             * Enum filters.
             */
            if (severity != null) {
                predicates.add(
                        cb.equal(
                                root.get("severity"),
                                severity
                        )
                );
            }

            if (priority != null) {
                predicates.add(
                        cb.equal(
                                root.get("priority"),
                                priority
                        )
                );
            }

            if (status != null) {
                predicates.add(
                        cb.equal(
                                root.get("status"),
                                status
                        )
                );
            }

            if (category != null) {
                predicates.add(
                        cb.equal(
                                root.get("category"),
                                category
                        )
                );
            }

            /*
             * Exact bug occurrence filter.
             */
            if (bugOccurrence != null) {
                predicates.add(
                        cb.equal(
                                root.get("bugOccurrence"),
                                bugOccurrence
                        )
                );
            }

            /*
             * Active/inactive filter.
             */
            if (isActive != null) {
                predicates.add(
                        cb.equal(
                                root.get("isActive"),
                                isActive
                        )
                );
            }

            /*
             * Resolved date range.
             *
             * >= start
             * < next day
             */
            if (resolvedFrom != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("resolvedAt"),
                                resolvedFrom
                        )
                );
            }

            if (resolvedTo != null) {
                predicates.add(
                        cb.lessThan(
                                root.get("resolvedAt"),
                                resolvedTo
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
