package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugAssignRequest;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.BugStatusRequest;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.BugRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.BugPutRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;

import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.NotificationResponse;
import xyz.mobi.testingautomationtool.entity.Bug;

import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import xyz.mobi.testingautomationtool.enums.NotificationStatus;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.postMapper.BugMapper;
import xyz.mobi.testingautomationtool.repository.BugRepository;

import xyz.mobi.testingautomationtool.repository.TestCaseRepository;
import xyz.mobi.testingautomationtool.repository.TestingExecutionRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.BugService;
import xyz.mobi.testingautomationtool.service.EmailService;
import xyz.mobi.testingautomationtool.service.NotificationService;
import xyz.mobi.testingautomationtool.utils.Utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class BugServiceImpl implements BugService {

    private final BugRepository bugRepository;
    private final UserRepository userRepository;
    private final BugMapper bugMapper;
    private final TestCaseRepository testCaseRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final Utils utils;
    private final TestingExecutionRepository testingExecutionRepository;


    @Override
    public BugResponse createBug(BugRequest request, Integer testCaseId) {

        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Test case not found with ID: " + testCaseId));

        User reportedBy = userRepository.findById(1)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: 1"));

        User assignedTo = null;

        if (request.getAssignedTo() != null) {
            assignedTo = userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Assigned user not found with ID: "
                                            + request.getAssignedTo()));
        }

        String bugFormatId = generateBugFormatId(testCaseId);

        Bug bug = bugMapper.toEntity(request);

        bug.setTestCase(testCase);
        bug.setBugFormatId(bugFormatId);
        bug.setFeature(testCase.getFeature());
        bug.setStatus(request.getStatus());
        bug.setReportedBy(reportedBy);
        bug.setExecutedBy(reportedBy);
        bug.setAssignedTo(assignedTo);
        bug.setBugOccurrence(1);
        bug.setComments(request.getComments());
        bug.setCategory(request.getCategory());
        bug.setActive(true);

        if(request.getStatus()== BugStatus.RESOLVED){
            bug.setResolvedAt(LocalDateTime.now());
        }

        if (request.getDynamicFields() != null) {
            bug.setDynamicFields(request.getDynamicFields());
        } else {
            bug.setDynamicFields(new HashMap<>());
        }

        Bug savedBug = bugRepository.save(bug);

        TestingExecution testingExecution = testingExecutionRepository.findById(testCaseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Test case not found with ID: " + testCaseId));

        testingExecution.setBugsCount(testingExecution.getBugsCount()+1);

        utils.trigger(testCase,reportedBy,bug);

        if(savedBug.getAssignedTo()!=null){

            String message =
                    "Bug " + savedBug.getBugFormatId()
                            + " has been assigned to you.";

            NotificationResponse notification = notificationService.createNotification(
                    savedBug.getExecutedBy().getUserId(),
                    savedBug.getBugId(),
                    savedBug.getAssignedTo().getUserId(),
                    message
            );

            try {
                emailService.sendBugAssignmentEmail(
                        savedBug.getAssignedTo().getEmail(),
                        savedBug
                );

                notificationService.updateStatus(
                        notification.getNotificationId(),
                        NotificationStatus.SENT
                );
            } catch (Exception exception) {
                notificationService.updateStatus(
                        notification.getNotificationId(),
                        NotificationStatus.FAIL
                );
            }
        }

        return bugMapper.toResponse(savedBug);
    }

    @Override
    @Transactional
    public String softDeleteBug(Integer bugId) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Bug not found with ID: " + bugId));

        if (!bug.isActive()) {
            throw new IllegalStateException("Bug with ID " + bugId + " is already deleted");
        }

        bug.setActive(false);
        bugRepository.save(bug);

        return "Bug deleted successfully";
    }

    @Override
    @Transactional
    public String hardDeleteBug(Integer bugId) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Bug not found with ID: " + bugId));

        bugRepository.delete(bug);

        return "Bug permanently deleted";
    }

    @Override
    @Transactional
    public BugResponse updateBug(Integer bugId, BugPutRequest request) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Bug not found with ID: " + bugId));

        if (!bug.isActive()) {
            throw new IllegalStateException("Cannot update disabled bug with ID: " + bugId);
        }

        User executedBy = userRepository.findById(1)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: 1"));

        bugMapper.updateEntity(bug, request);

        bug.setExecutedBy(executedBy);


        if (request.getStatus() == BugStatus.RESOLVED) {
            if (bug.getResolvedAt() == null) {
                bug.setResolvedAt(LocalDateTime.now());
            }
        } else {
            bug.setResolvedAt(null);
        }


        bug = bugRepository.save(bug);

        if(bug.getAssignedTo() != request.getAssignedTo()){
            // notification service call
        }

        String message =
                "Bug " + bug.getBugFormatId()
                        + " has been assigned to you.";

        notificationService.createNotification(
                bug.getReportedBy().getUserId(),
                request.getAssignedTo().getUserId(),
                bug.getBugId(),
                message
        );

        return bugMapper.toResponse(bug);
    }

    @Override
    @Transactional
    public BugResponse assignBug(Integer bugId, BugAssignRequest request) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Bug not found with ID: " + bugId));

        if (!bug.isActive()) {
            throw new IllegalStateException("Cannot assign disabled bug with ID: " + bugId);
        }

        User assignedUser = userRepository.findById(request.getAssignedTo())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with ID: " + request.getAssignedTo()));

        bug.setAssignedTo(assignedUser);
        bug = bugRepository.save(bug);

        String message = "Bug " + bug.getBugFormatId() + " has been assigned to you.";


        User reportedBy = userRepository.findById(1)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: 1"));



        // we will assign the previous assign now assigned user not equal we should send two mails


        NotificationResponse notification = notificationService.createNotification(
                reportedBy.getUserId(),
                assignedUser.getUserId(),
                bug.getBugId(),
                message
        );

        try {
            emailService.sendBugAssignmentEmail(
                    assignedUser.getEmail(),
                    bug
            );

            notificationService.updateStatus(
                    notification.getNotificationId(),
                    NotificationStatus.SENT
            );
        } catch (Exception exception) {
            notificationService.updateStatus(
                    notification.getNotificationId(),
                    NotificationStatus.FAIL
            );
        }

        return bugMapper.toResponse(bug);
    }

    @Override
    public BugResponse updateStatus(Integer bugId, BugStatusRequest request) {

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Bug not found with ID: " + bugId));

        if (!bug.isActive()) {
            throw new IllegalStateException("Cannot update status of disabled bug with ID: " + bugId);
        }

        BugStatus newStatus = request.getStatus();
        bug.setStatus(newStatus);

        if (newStatus == BugStatus.RESOLVED) {
            bug.setResolvedAt(LocalDateTime.now());
        } else {
            bug.setResolvedAt(null);
        }

        if(newStatus == BugStatus.REOPENED){

            // notificataion call to assignee
        }

        bug = bugRepository.save(bug);

        utils.trigger(bug.getTestCase(), bug.getReportedBy(),bug);

        return bugMapper.toResponse(bug);
    }

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
    public List<BugResponse> getByAll() {
        return bugRepository.findByActiveTrue()
                .stream()
                .map(bugMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BugResponse> getAllBugs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bugRepository.findByActiveTrue(pageable)
                .map(bugMapper::toResponse);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public List<BugResponse> getByTestcaseId(Integer testcaseId) {
//        return bugRepository.findByTestCase_TestcaseIdAndActiveTrueOrderByBugIdAsc(testcaseId)
//                .stream()
//                .map(bugMapper::toResponse)
//                .toList();
//    }

    @Override
    @Transactional(readOnly = true)
    public Page<BugResponse> getByTestcaseId(Integer testcaseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bugRepository.findByTestCase_TestcaseIdAndActiveTrue(testcaseId, pageable)
                .map(bugMapper::toResponse);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public List<BugResponse> getByFeatureId(Integer featureId) {
//        return bugRepository.findByFeature_FeatureIdAndActiveTrueOrderByBugIdAsc(featureId)
//                .stream()
//                .map(bugMapper::toResponse)
//                .toList();
//    }

    @Override
    @Transactional(readOnly = true)
    public Page<BugResponse> getByFeatureId(Integer featureId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bugRepository.findByFeature_FeatureIdAndActiveTrue(featureId, pageable)
                .map(bugMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BugResponse> getByStatus(BugStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bugRepository.findByStatusAndActiveTrue(status, pageable)
                .map(bugMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BugResponse> getByAssignedTo(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bugRepository.findByAssignedTo_UserIdAndActiveTrue(userId, pageable)
                .map(bugMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BugResponse> getBySeverity(BugSeverity severity, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bugRepository.findBySeverityAndActiveTrue(severity, pageable)
                .map(bugMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BugResponse> getByPriority(BugPriority priority, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bugRepository.findByPriorityAndActiveTrue(priority, pageable)
                .map(bugMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BugResponse> getByReportedBy(Integer id, int page, int size) {
        User user = userRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("User is not present for this Id:"+id));
        Pageable pageable = PageRequest.of(page,size);
        return bugRepository.findByReportedByAndActiveTrue(user,pageable).map(bugMapper::toResponse);
    }


    private String generateBugFormatId(Integer testcaseId) {
        Optional<Bug> latestBug = bugRepository.findTopByTestCase_TestcaseIdOrderByBugIdDesc(testcaseId);

        int nextNumber = latestBug
                .map(bug -> {
                    String bugFormatId = bug.getBugFormatId();
                    String numberPart = bugFormatId.substring(bugFormatId.lastIndexOf("-") + 1);
                    return Integer.parseInt(numberPart) + 1;
                })
                .orElse(1);

        return String.format("BUG-%03d", nextNumber);
    }
}