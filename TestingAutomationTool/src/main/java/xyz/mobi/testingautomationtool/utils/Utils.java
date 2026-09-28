package xyz.mobi.testingautomationtool.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.BugHistory;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.repository.AuditLogRepository;
import xyz.mobi.testingautomationtool.repository.BugHistoryRepository;

import java.time.Instant;


@Component
@RequiredArgsConstructor
public class Utils {

    private final BugHistoryRepository bugHistoryRepository;

    public void bugHistory(Bug bug, User executedBy) {

        BugHistory history = BugHistory.builder()
                .bug(bug)
                .executedBy(executedBy.getUserId())
                .bugStatus(bug.getStatus())
                .assignedTo(bug.getAssignedTo().getUserId())
                .createdAt(Instant.now())
                .build();

        bugHistoryRepository.save(history);
    }

}
