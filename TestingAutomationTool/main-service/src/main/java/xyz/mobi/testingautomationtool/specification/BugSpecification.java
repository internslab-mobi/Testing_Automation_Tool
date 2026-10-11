package xyz.mobi.testingautomationtool.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
            Instant resolvedTo,
            String executedBy,
            String assignedTo,
            String updatedBy
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
             * Global keyword search across text fields, test case, feature, and user relations.
             */
            if (keyword != null && !keyword.isBlank()) {

                String search = "%" + keyword.trim().toLowerCase() + "%";
                List<Predicate> keywordPredicates = new ArrayList<>();

                // Direct Bug fields
                keywordPredicates.add(cb.like(cb.lower(root.get("bugFormatId")), search));
                keywordPredicates.add(cb.like(cb.lower(root.get("title")), search));
                keywordPredicates.add(cb.like(cb.lower(root.get("description")), search));
                keywordPredicates.add(cb.like(cb.lower(root.get("comments")), search));

                // TestCase joins
                Join<Bug, TestCase> testCaseJoin = root.join("testCase", JoinType.LEFT);
                keywordPredicates.add(cb.like(cb.lower(testCaseJoin.get("testcaseFormatId")), search));
                keywordPredicates.add(cb.like(cb.lower(testCaseJoin.get("title")), search));
                keywordPredicates.add(cb.like(cb.toString(testCaseJoin.get("testcaseId")), search));

                // Feature joins
                Join<Bug, Feature> featureJoin = root.join("feature", JoinType.LEFT);
                keywordPredicates.add(cb.like(cb.lower(featureJoin.get("featureName")), search));
                keywordPredicates.add(cb.like(cb.toString(featureJoin.get("featureId")), search));

                // User relations
                Join<Bug, User> reportedUser = root.join("reportedBy", JoinType.LEFT);
                keywordPredicates.add(cb.like(cb.lower(reportedUser.get("username")), search));
                keywordPredicates.add(cb.like(cb.lower(reportedUser.get("fullName")), search));

                Join<Bug, User> assignedUser = root.join("assignedTo", JoinType.LEFT);
                keywordPredicates.add(cb.like(cb.lower(assignedUser.get("username")), search));
                keywordPredicates.add(cb.like(cb.lower(assignedUser.get("fullName")), search));

                Join<Bug, User> executedUser = root.join("executedBy", JoinType.LEFT);
                keywordPredicates.add(cb.like(cb.lower(executedUser.get("username")), search));
                keywordPredicates.add(cb.like(cb.lower(executedUser.get("fullName")), search));

                Join<Bug, User> updatedUser = root.join("updatedBy", JoinType.LEFT);
                keywordPredicates.add(cb.like(cb.lower(updatedUser.get("username")), search));
                keywordPredicates.add(cb.like(cb.lower(updatedUser.get("fullName")), search));

                predicates.add(
                        cb.or(keywordPredicates.toArray(new Predicate[0]))
                );
            }

            /*
             * Severity.
             */
            if (severity != null) {
                predicates.add(cb.equal(root.get("severity"), severity));
            }

            /*
             * Priority.
             */
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            /*
             * Status.
             */
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            /*
             * Category.
             */
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            /*
             * Bug occurrence.
             */
            if (bugOccurrence != null) {
                predicates.add(cb.equal(root.get("bugOccurrence"), bugOccurrence));
            }

            /*
             * Active / inactive.
             */
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            /*
             * Executed by user (matches username or full name).
             */
            if (executedBy != null && !executedBy.isBlank()) {
                Join<Bug, User> executedUser = root.join("executedBy", JoinType.LEFT);
                String searchExecuted = "%" + executedBy.trim().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(executedUser.get("username")), searchExecuted),
                                cb.like(cb.lower(executedUser.get("fullName")), searchExecuted)
                        )
                );
            }

            /*
             * Assigned to user (matches username or full name).
             */
            if (assignedTo != null && !assignedTo.isBlank()) {
                Join<Bug, User> assignedUser = root.join("assignedTo", JoinType.LEFT);
                String searchAssigned = "%" + assignedTo.trim().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(assignedUser.get("username")), searchAssigned),
                                cb.like(cb.lower(assignedUser.get("fullName")), searchAssigned)
                        )
                );
            }

            /*
             * Updated by user (matches username or full name).
             */
            if (updatedBy != null && !updatedBy.isBlank()) {
                Join<Bug, User> updatedUser = root.join("updatedBy", JoinType.LEFT);
                String searchUpdated = "%" + updatedBy.trim().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(updatedUser.get("username")), searchUpdated),
                                cb.like(cb.lower(updatedUser.get("fullName")), searchUpdated)
                        )
                );
            }

            /*
             * Resolved from date.
             */
            if (resolvedFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("resolvedAt"), resolvedFrom));
            }

            /*
             * Resolved to date.
             */
            if (resolvedTo != null) {
                predicates.add(cb.lessThan(root.get("resolvedAt"), resolvedTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}