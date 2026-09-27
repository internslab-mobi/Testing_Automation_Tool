package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;

import xyz.mobi.testingautomationtool.entity.Bug;

import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.getMapper.BugMapper;
import xyz.mobi.testingautomationtool.repository.BugRepository;

import xyz.mobi.testingautomationtool.service.BugService;
import xyz.mobi.testingautomationtool.specification.BugSpecification;

import java.time.*;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class BugServiceImpl implements BugService {

    private final BugRepository bugRepository;
    private final BugMapper bugMapper;

    @Override
    @Transactional(readOnly = true)
    public BugResponse getById(Integer bugId) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Bug not found with ID: " + bugId));

        if (!bug.isActive()) {
            throw new ResourceNotFoundException("The bug has been removed with ID: " + bugId);
        }

        return bugMapper.toResponse(bug);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<BugResponse> getAllBugs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bugRepository.findByActiveTrue(pageable)
                .map(bugMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BugResponse> globalSearch(
            String keyword,
            BugSeverity severity,
            BugPriority priority,
            BugStatus status,
            BugCategory category,
            Integer bugOccurrence,
            Boolean isActive,
            LocalDate resolvedFrom,
            LocalDate resolvedTo,
            String timeZone,
            Pageable pageable
    ) {

        if (resolvedFrom != null
                && resolvedTo != null
                && resolvedFrom.isAfter(resolvedTo)) {

            throw new IllegalArgumentException(
                    "Resolved from date cannot be after resolved to date"
            );
        }

        ZoneId zoneId = ZoneOffset.UTC;

        if (timeZone != null && !timeZone.isBlank()) {

            try {
                zoneId = ZoneId.of(timeZone);

            } catch (DateTimeException exception) {

                throw new IllegalArgumentException(
                        "Invalid timezone: " + timeZone
                );
            }
        }

        Instant resolvedFromInstant = null;
        Instant resolvedToInstant = null;

        if (resolvedFrom != null) {

            resolvedFromInstant = resolvedFrom
                    .atStartOfDay(zoneId)
                    .toInstant();
        }

        if (resolvedTo != null) {

            resolvedToInstant = resolvedTo
                    .plusDays(1)
                    .atStartOfDay(zoneId)
                    .toInstant();
        }

        Specification<Bug> specification =
                BugSpecification.search(
                        keyword,
                        severity,
                        priority,
                        status,
                        category,
                        bugOccurrence,
                        isActive,
                        resolvedFromInstant,
                        resolvedToInstant
                );

        Page<Bug> bugs =
                bugRepository.findAll(
                        specification,
                        pageable
                );

        return bugs.map(bugMapper::toResponse);
    }
}