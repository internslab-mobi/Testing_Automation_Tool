package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import xyz.mobi.testingautomationtool.dto.AdminDTO.CreateManagerRequest;
import xyz.mobi.testingautomationtool.dto.AdminDTO.ManagerResponse;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.entity.Project;
import xyz.mobi.testingautomationtool.entity.Role;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.UserRole;
import xyz.mobi.testingautomationtool.exception.DuplicateResourceException;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.ManagerMapper;
import xyz.mobi.testingautomationtool.repository.BugRepository;
import xyz.mobi.testingautomationtool.repository.FeatureRepository;
import xyz.mobi.testingautomationtool.repository.ProjectRepository;
import xyz.mobi.testingautomationtool.repository.RoleRepository;
import xyz.mobi.testingautomationtool.repository.TestCaseRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.AdminService;
import xyz.mobi.testingautomationtool.service.AuthService;
import xyz.mobi.testingautomationtool.service.EmailService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final ProjectRepository projectRepository;
    private final FeatureRepository featureRepository;
    private final TestCaseRepository testCaseRepository;
    private final BugRepository bugRepository;

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final ManagerMapper managerMapper;


    @Override
    @Transactional
    public ManagerResponse createManager(CreateManagerRequest request) {

        User currentAdmin = authService.getCurrentUser();

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                    "Username already exists: " + request.getUsername()
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already exists: " + request.getEmail()
            );
        }

        Role managerRole = roleRepository
                .findByRole("MANAGER")
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "MANAGER role not found"
                        ));


        String rawPassword = request.getPassword();

        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        User manager = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(encodedPassword)
                .fullName(request.getFullName())
                .designation(request.getDesignation())
                .skills(request.getSkills())
                .isActive(true)
                .role(managerRole)
//                .createdBy(currentAdmin)
//                .updatedBy(currentAdmin)
                .build();

        User savedManager = userRepository.save(manager);

        emailService.sendManagerCredentials(
                savedManager.getEmail(),
                savedManager.getUsername(),
                rawPassword
        );

        return managerMapper.toResponse(savedManager);

    }

    @Transactional
    @Override
    public ManagerResponse updateManager(
            Integer userId,
            CreateManagerRequest request) {

        User manager = userRepository
                .findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Manager not found with id: " + userId
                        )
                );

        if (!"MANAGER".equals(manager.getRole().getRole())) {
            throw new IllegalArgumentException(
                    "User is not a MANAGER"
            );
        }

        if (!manager.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {

            throw new DuplicateResourceException(
                    "Username already exists: " + request.getUsername()
            );
        }

        if (!manager.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {

            throw new DuplicateResourceException(
                    "Email already exists: " + request.getEmail()
            );
        }

        manager.setUsername(request.getUsername());
        manager.setEmail(request.getEmail());
        manager.setFullName(request.getFullName());
        manager.setDesignation(request.getDesignation());
        manager.setSkills(request.getSkills());

        User updatedManager = userRepository.save(manager);

        return managerMapper.toResponse(updatedManager);
    }

    @Transactional
    @Override
    public String patchManager(
            Integer userId,
            CreateManagerRequest request) {

        User manager = userRepository
                .findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Manager not found with id: " + userId
                        )
                );

        if (!"MANAGER".equals(manager.getRole().getRole())) {
            throw new IllegalArgumentException(
                    "User is not a MANAGER"
            );
        }

        if (request.getUsername() != null
                && !request.getUsername().equals(manager.getUsername())) {

            if (userRepository.existsByUsername(request.getUsername())) {
                throw new DuplicateResourceException(
                        "Username already exists: " + request.getUsername()
                );
            }

            manager.setUsername(request.getUsername());
        }

        if (request.getEmail() != null
                && !request.getEmail().equals(manager.getEmail())) {

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException(
                        "Email already exists: " + request.getEmail()
                );
            }

            manager.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            manager.setFullName(request.getFullName());
        }

        if (request.getDesignation() != null) {
            manager.setDesignation(request.getDesignation());
        }

        if (request.getSkills() != null) {
            manager.setSkills(request.getSkills());
        }

        User updatedManager = userRepository.save(manager);

        return "Manager updated successfully";
    }

    @Transactional(readOnly = true)
    @Override
    public ManagerResponse getUserById(Integer userId) {

        User user = userRepository
                .findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        )
                );

        return managerMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ManagerResponse> getAllUsers() {

        return userRepository
                .findAllByIsActiveTrue()
                .stream()
                .map(managerMapper::toResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    @Override
    public List<ManagerResponse> getUsersByRole(UserRole role) {

        return userRepository
                .findAllByRoleRoleAndIsActiveTrue(role.name())
                .stream()
                .map(managerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void softDeleteUser(Integer userId) {

        User currentAdmin = authService.getCurrentUser();

        User user = userRepository
                .findByUserIdAndIsActiveFalse(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + userId
                        ));


        if (user.getUserId().equals(currentAdmin.getUserId())) {
            throw new IllegalArgumentException(
                    "Admin cannot delete himself"
            );
        }

        user.setActive(false);

        userRepository.save(user);
    }


    @Override
    @Transactional
    public void hardDeleteUser(Integer userId) {

        User currentAdmin = authService.getCurrentUser();

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + userId
                        ));

        if (user.getUserId().equals(currentAdmin.getUserId())) {
            throw new IllegalArgumentException(
                    "Admin cannot delete himself"
            );
        }

        userRepository.delete(user);
    }


    @Override
    @Transactional
    public void hardDeleteProject(Integer projectId) {

        Project project = projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with ID: " + projectId
                        ));

        projectRepository.delete(project);
    }


    @Override
    @Transactional
    public void hardDeleteFeature(Integer featureId) {

        Feature feature = featureRepository
                .findById(featureId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feature not found with ID: " + featureId
                        ));

        featureRepository.delete(feature);
    }


    @Override
    @Transactional
    public void hardDeleteTestCase(Integer testCaseId) {

        TestCase testCase = testCaseRepository
                .findById(testCaseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Test case not found with ID: " + testCaseId
                        ));

        testCaseRepository.delete(testCase);
    }


    @Override
    @Transactional
    public void hardDeleteBug(Integer bugId) {

        Bug bug = bugRepository
                .findById(bugId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bug not found with ID: " + bugId
                        ));

        bugRepository.delete(bug);
    }
}