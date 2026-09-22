package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;

import xyz.mobi.testingautomationtool.entity.Bug;

import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.getMapper.BugMapper;
import xyz.mobi.testingautomationtool.repository.BugRepository;

import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.BugService;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class BugServiceImpl implements BugService {

    private final BugRepository bugRepository;
    private final UserRepository userRepository;
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

    @Override
    @Transactional(readOnly = true)
    public List<BugResponse> getByTestcaseId(Integer testcaseId) {
        return bugRepository.findByTestCase_TestcaseIdAndActiveTrueOrderByBugIdAsc(testcaseId)
                .stream()
                .map(bugMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BugResponse> getByTestcaseId(Integer testcaseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bugRepository.findByTestCase_TestcaseIdAndActiveTrue(testcaseId, pageable)
                .map(bugMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BugResponse> getByFeatureId(Integer featureId) {
        return bugRepository.findByFeature_FeatureIdAndActiveTrueOrderByBugIdAsc(featureId)
                .stream()
                .map(bugMapper::toResponse)
                .toList();
    }

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




//    @Override
//    public BugResponse createBug(BugRequest request, Integer testCaseId) {
//
//        TestCase testCase = testCaseRepository.findById(testCaseId)
//                .orElseThrow(() -> new ResourceNotFoundException("Test case not found with ID: " + testCaseId));
//
//        User reportedBy = userRepository.findById(1)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: 1"));
//
//        User assignedTo = null;
//        if (request.getAssignedTo() != null) {
//            assignedTo = userRepository.findById(request.getAssignedTo())
//                    .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found with ID: " + request.getAssignedTo()));
//        }
//
//        Bug bug = new Bug();
//        bug.setBugFormatId(request.getBugFormatId());
//        bug.setTestCase(testCase);
//        bug.setFeature(testCase.getFeature());
//        bug.setTitle(request.getTitle());
//        bug.setDescription(request.getDescription());
//        bug.setSeverity(request.getSeverity());
//        bug.setPriority(request.getPriority());
//        bug.setReportedBy(reportedBy);
//        bug.setAssignedTo(assignedTo);
//        bug.setBugOccurrence(1);
//        bug.setActive(true);
//
//        Bug savedBug = bugRepository.save(bug);
//
//        return bugMapper.toResponse(savedBug);
//    }
//
//    @Override
//    @Transactional
//    public String softDeleteBug(Integer bugId) {
//
//        Bug bug = bugRepository.findById(bugId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Bug not found with ID: " + bugId));
//
//        if (!bug.isActive()) {
//            throw new IllegalStateException("Bug with ID " + bugId + " is already deleted");
//        }
//
//        bug.setActive(false);
//        bugRepository.save(bug);
//
//        return "Bug deleted successfully";
//    }
//
//    @Override
//    @Transactional
//    public String hardDeleteBug(Integer bugId) {
//
//        Bug bug = bugRepository.findById(bugId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Bug not found with ID: " + bugId));
//
//        bugRepository.delete(bug);
//
//        return "Bug permanently deleted";
//    }
//
//    @Override
//    @Transactional
//    public BugResponse updateBug(Integer bugId, BugPutRequest request) {
//
//        Bug bug = bugRepository.findById(bugId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Bug not found with ID: " + bugId));
//
//        if (!bug.isActive()) {
//            throw new IllegalStateException("Cannot update disabled bug with ID: " + bugId);
//        }
//
//        bug.setTitle(request.getTitle());
//        bug.setDescription(request.getDescription());
//        bug.setPriority(request.getPriority());
//        bug.setSeverity(request.getSeverity());
//        bug.setStatus(request.getStatus());
//
//        if (request.getStatus() == BugStatus.RESOLVED) {
//            if (bug.getResolvedAt() == null) {
//                bug.setResolvedAt(LocalDateTime.now());
//            }
//        } else {
//            bug.setResolvedAt(null);
//        }
//
//        bug = bugRepository.save(bug);
//
//        return bugMapper.toResponse(bug);
//    }
//
//    @Override
//    @Transactional
//    public BugResponse assignBug(Integer bugId, BugAssignRequest request) {
//
//        Bug bug = bugRepository.findById(bugId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Bug not found with ID: " + bugId));
//
//        if (!bug.isActive()) {
//            throw new IllegalStateException("Cannot assign disabled bug with ID: " + bugId);
//        }
//
//        User assignedUser = userRepository.findById(request.getAssignedTo())
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("User not found with ID: " + request.getAssignedTo()));
//
//        bug.setAssignedTo(assignedUser);
//        bug = bugRepository.save(bug);
//
//        String message = "Bug " + bug.getBugFormatId() + " has been assigned to you.";
//
//        NotificationResponse notification = notificationService.createNotification(
//                assignedUser.getUserId(),
//                bug.getBugId(),
//                message
//        );
//
//        try {
//            emailService.sendBugAssignmentEmail(
//                    assignedUser.getEmail(),
//                    bug
//            );
//
//            notificationService.updateStatus(
//                    notification.getNotificationId(),
//                    NotificationStatus.SENT
//            );
//        } catch (Exception exception) {
//            notificationService.updateStatus(
//                    notification.getNotificationId(),
//                    NotificationStatus.FAIL
//            );
//        }
//
//        return bugMapper.toResponse(bug);
//    }
//
//    @Override
//    public BugResponse updateStatus(Integer bugId, BugStatusRequest request) {
//
//        Bug bug = bugRepository.findById(bugId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Bug not found with ID: " + bugId));
//
//        if (!bug.isActive()) {
//            throw new IllegalStateException("Cannot update status of disabled bug with ID: " + bugId);
//        }
//
//        BugStatus newStatus = request.getStatus();
//        bug.setStatus(newStatus);
//
//        if (newStatus == BugStatus.RESOLVED) {
//            bug.setResolvedAt(LocalDateTime.now());
//        } else {
//            bug.setResolvedAt(null);
//        }
//
//        bug = bugRepository.save(bug);
//
//        return bugMapper.toResponse(bug);
//    }



//    @Override
//    @Transactional
//    public BugResponse createReoccurrence(Integer bugId) {
//
//        Bug oldBug = bugRepository.findById(bugId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Bug not found with ID: " + bugId));
//
//        Bug latestBug = findLatestOccurrence(oldBug);
//
//        Integer currentOccurrence = latestBug.getBugOccurrence() == null ? 1 : latestBug.getBugOccurrence();
//        Integer nextOccurrence = currentOccurrence + 1;
//
//        String bugFormatId = generateBugFormatId(latestBug.getTestCase().getTestcaseId());
//
//        Bug newBug = Bug.builder()
//                .bugFormatId(bugFormatId)
//                .testCase(latestBug.getTestCase())
//                .feature(latestBug.getFeature())
//                .title(latestBug.getTitle())
//                .description(latestBug.getDescription())
//                .severity(latestBug.getSeverity())
//                .priority(latestBug.getPriority())
//                .status(BugStatus.OPEN)
//                .reportedBy(latestBug.getReportedBy())
//                .assignedTo(latestBug.getAssignedTo())
//                .resolvedAt(null)
//                .bugReoccurred(latestBug)
//                .bugOccurrence(nextOccurrence)
//                .active(true)
//                .build();
//
//        newBug = bugRepository.save(newBug);
//
//        return bugMapper.toResponse(newBug);
//    }
//
//    private Bug findLatestOccurrence(Bug bug) {
//        Bug current = bug;
//        while (true) {
//            Optional<Bug> nextBug = bugRepository.findByBugReoccurred(current);
//            if (nextBug.isEmpty()) {
//                return current;
//            }
//            current = nextBug.get();
//        }
//    }
//
//    private String generateBugFormatId(Integer testcaseId) {
//        Optional<Bug> latestBug = bugRepository.findTopByTestCase_TestcaseIdOrderByBugIdDesc(testcaseId);
//
//        int nextNumber = latestBug
//                .map(bug -> {
//                    String bugFormatId = bug.getBugFormatId();
//                    String numberPart = bugFormatId.substring(bugFormatId.lastIndexOf("-") + 1);
//                    return Integer.parseInt(numberPart) + 1;
//                })
//                .orElse(1);
//
//        return String.format("BUG-%03d", nextNumber);
//    }
}