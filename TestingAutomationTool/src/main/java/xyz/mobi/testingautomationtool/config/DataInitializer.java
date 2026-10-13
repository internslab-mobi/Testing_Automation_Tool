//package xyz.mobi.testingautomationtool.config;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//import xyz.mobi.testingautomationtool.entity.Error;
//import xyz.mobi.testingautomationtool.entity.Role;
//import xyz.mobi.testingautomationtool.repository.ErrorDataRepository;
//import xyz.mobi.testingautomationtool.repository.RoleRepository;
//
//import java.util.Arrays;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class DataInitializer implements CommandLineRunner {
//
//    private final RoleRepository roleRepository;
//    private final ErrorDataRepository errorDataRepository;
//
//    @Override
//    public void run(String... args) {
//        initializeRoles();
//        initializeExceptionMappings();
//    }
//
//    private void initializeRoles() {
//        List<String> defaultRoles = Arrays.asList("ADMIN", "MANAGER", "DEVELOPER", "TESTER");
//
//        for (String roleName : defaultRoles) {
//            if (!roleRepository.existsByRole(roleName)) {
//                Role role = Role.builder()
//                        .role(roleName)
//                        .build();
//                roleRepository.save(role);
//                log.info("Initialized default role: {}", roleName);
//            }
//        }
//    }
//
//    private void initializeExceptionMappings() {
//        Map<String, String> defaultExceptionMappings = new LinkedHashMap<>();
//        // Core 14 mappings specified by system
//        defaultExceptionMappings.put("ResourceNotFoundException", "ERR_001");
//        defaultExceptionMappings.put("IllegalArgumentException", "ERR_002");
//        defaultExceptionMappings.put("IllegalStateException", "ERR_003");
//        defaultExceptionMappings.put("MethodArgumentNotValidException", "ERR_004");
//        defaultExceptionMappings.put("EmailNotFoundException", "ERR_005");
//        defaultExceptionMappings.put("DataIntegrityViolationException", "ERR_006");
//        defaultExceptionMappings.put("NullPointerException", "ERR_007");
//        defaultExceptionMappings.put("GlobalException", "ERR_008");
//        defaultExceptionMappings.put("Exception", "ERR_999");
//        defaultExceptionMappings.put("ExcelValidationException", "ERR_010");
//        defaultExceptionMappings.put("ExcelProcessingException", "ERR_011");
//        defaultExceptionMappings.put("MaxUploadSizeExceededException", "ERR_012");
//        defaultExceptionMappings.put("ObjectOptimisticLockingFailureException", "ERR_013");
//        defaultExceptionMappings.put("DuplicateResourceException", "ERR_014");
//
//        // Additional Custom & Security mappings for complete coverage
//        defaultExceptionMappings.put("AttachmentProcessingException", "ERR_015");
//        defaultExceptionMappings.put("FileProcessingException", "ERR_016");
//        defaultExceptionMappings.put("AccessDeniedException", "ERR_017");
//        defaultExceptionMappings.put("BadCredentialsException", "ERR_018");
//        defaultExceptionMappings.put("AuthenticationException", "ERR_019");
//        defaultExceptionMappings.put("ExpiredJwtException", "ERR_020");
//        defaultExceptionMappings.put("EmailSendingException", "ERR_021");
//        defaultExceptionMappings.put("AccountDisabledException", "ERR_022");
//        defaultExceptionMappings.put("InvalidTokenException", "ERR_023");
//
//        for (Map.Entry<String, String> entry : defaultExceptionMappings.entrySet()) {
//            String exceptionName = entry.getKey();
//            String errorCode = entry.getValue();
//
//            if (!errorDataRepository.existsByExceptionName(exceptionName)) {
//                Error error = Error.builder()
//                        .exceptionName(exceptionName)
//                        .errorCode(errorCode)
//                        .build();
//                errorDataRepository.save(error);
//                log.info("Initialized exception mapping: {} -> {}", exceptionName, errorCode);
//            }
//        }
//    }
//}
