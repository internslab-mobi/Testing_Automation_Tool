package xyz.mobi.testingautomationtool.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

import java.util.ArrayList;
import java.util.List;

public final class TestCaseSpecification {

    private TestCaseSpecification() {
    }

    public static Specification<TestCase> search(
            String keyword,
            Integer featureId,
            TestCaseStatus status,
            TestType type,
            TestPriority priority) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Exclude deleted test cases
            predicates.add(
                    cb.isFalse(root.get("isDeleted"))
            );

            // Feature filter
            if (featureId != null) {
                predicates.add(
                        cb.equal(
                                root.get("feature").get("featureId"),
                                featureId
                        )
                );
            }

            // Status filter
            if (status != null) {
                predicates.add(
                        cb.equal(
                                root.get("testcaseStatus"),
                                status
                        )
                );
            }

            // Type filter
            if (type != null) {
                predicates.add(
                        cb.equal(
                                root.get("testType"),
                                type
                        )
                );
            }

            // Priority filter
            if (priority != null) {
                predicates.add(
                        cb.equal(
                                root.get("testPriority"),
                                priority
                        )
                );
            }

            // Keyword search
            if (keyword != null && !keyword.isBlank()) {

                String pattern =
                        "%" + keyword.trim().toLowerCase() + "%";

                Predicate titleMatch =
                        cb.like(
                                cb.lower(root.get("title")),
                                pattern
                        );

                Predicate formatMatch =
                        cb.like(
                                cb.lower(root.get("testcaseFormatId")),
                                pattern
                        );

                predicates.add(
                        cb.or(titleMatch, formatMatch)
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}