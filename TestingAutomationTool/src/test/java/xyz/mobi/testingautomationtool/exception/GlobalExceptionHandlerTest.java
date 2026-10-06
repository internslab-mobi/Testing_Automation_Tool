package xyz.mobi.testingautomationtool.exception;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import xyz.mobi.testingautomationtool.entity.Error;
import xyz.mobi.testingautomationtool.repository.ErrorDataRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private ErrorDataRepository errorDataRepository;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        List<Error> mockErrors = Arrays.asList(
                Error.builder().id(1).exceptionName("ResourceNotFoundException").errorCode("ERR_001").build(),
                Error.builder().id(2).exceptionName("IllegalArgumentException").errorCode("ERR_002").build(),
                Error.builder().id(3).exceptionName("IllegalStateException").errorCode("ERR_003").build(),
                Error.builder().id(4).exceptionName("MethodArgumentNotValidException").errorCode("ERR_004").build(),
                Error.builder().id(5).exceptionName("EmailNotFoundException").errorCode("ERR_005").build(),
                Error.builder().id(6).exceptionName("DataIntegrityViolationException").errorCode("ERR_006").build(),
                Error.builder().id(7).exceptionName("NullPointerException").errorCode("ERR_007").build(),
                Error.builder().id(8).exceptionName("GlobalException").errorCode("ERR_008").build(),
                Error.builder().id(9).exceptionName("Exception").errorCode("ERR_999").build(),
                Error.builder().id(10).exceptionName("ExcelValidationException").errorCode("ERR_010").build(),
                Error.builder().id(11).exceptionName("ExcelProcessingException").errorCode("ERR_011").build(),
                Error.builder().id(12).exceptionName("MaxUploadSizeExceededException").errorCode("ERR_012").build(),
                Error.builder().id(13).exceptionName("ObjectOptimisticLockingFailureException").errorCode("ERR_013").build(),
                Error.builder().id(14).exceptionName("DuplicateResourceException").errorCode("ERR_014").build(),
                Error.builder().id(15).exceptionName("AttachmentProcessingException").errorCode("ERR_015").build(),
                Error.builder().id(16).exceptionName("FileProcessingException").errorCode("ERR_016").build(),
                Error.builder().id(17).exceptionName("AccessDeniedException").errorCode("ERR_017").build(),
                Error.builder().id(18).exceptionName("BadCredentialsException").errorCode("ERR_018").build(),
                Error.builder().id(19).exceptionName("AuthenticationException").errorCode("ERR_019").build(),
                Error.builder().id(20).exceptionName("ExpiredJwtException").errorCode("ERR_020").build(),
                Error.builder().id(21).exceptionName("EmailSendingException").errorCode("ERR_021").build(),
                Error.builder().id(22).exceptionName("AccountDisabledException").errorCode("ERR_022").build(),
                Error.builder().id(23).exceptionName("InvalidTokenException").errorCode("ERR_023").build()
        );

        when(errorDataRepository.findAll()).thenReturn(mockErrors);
        globalExceptionHandler.loadExceptionMappings();
    }

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("ERR_001", response.getBody().getErrorCode());
        assertEquals("Resource not found", response.getBody().getErrorMessage());
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ERR_002", response.getBody().getErrorCode());
        assertEquals("Invalid argument", response.getBody().getErrorMessage());
    }

    @Test
    void testHandleIllegalStateException() {
        IllegalStateException ex = new IllegalStateException("Illegal state");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalStateException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ERR_003", response.getBody().getErrorCode());
        assertEquals("Illegal state", response.getBody().getErrorMessage());
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be blank");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ERR_004", response.getBody().getErrorCode());
        assertEquals("must not be blank", response.getBody().getErrorMessage());
    }

    @Test
    void testHandleEmailNotFoundException() {
        EmailNotFoundException ex = new EmailNotFoundException("Email not found");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleEmailNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("ERR_005", response.getBody().getErrorCode());
        assertEquals("Email not found", response.getBody().getErrorMessage());
    }

    @Test
    void testHandleDataIntegrityViolationException() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Integrity violation");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDataIntegrityViolationException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("ERR_006", response.getBody().getErrorCode());
    }

    @Test
    void testHandleNullPointerException() {
        NullPointerException ex = new NullPointerException("Null value");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNullPointerException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("ERR_007", response.getBody().getErrorCode());
    }

    @Test
    void testHandleGlobalException() {
        GlobalException ex = new GlobalException("Application error");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGlobalException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("ERR_008", response.getBody().getErrorCode());
    }

    @Test
    void testHandleExcelValidationException() {
        ExcelValidationException ex = new ExcelValidationException("Excel format error");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleExcelValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ERR_010", response.getBody().getErrorCode());
    }

    @Test
    void testHandleExcelProcessingException() {
        ExcelProcessingException ex = new ExcelProcessingException("Excel read failure");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleExcelProcessingException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ERR_011", response.getBody().getErrorCode());
    }

    @Test
    void testHandleMaxUploadSizeExceededException() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(50000000);
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMaxUploadSizeExceededException(ex);

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertEquals("ERR_012", response.getBody().getErrorCode());
    }

    @Test
    void testHandleObjectOptimisticLockingFailureException() {
        ObjectOptimisticLockingFailureException ex = new ObjectOptimisticLockingFailureException("Entity", 1);
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleOptimisticLockingException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("ERR_013", response.getBody().getErrorCode());
    }

    @Test
    void testHandleDuplicateResourceException() {
        DuplicateResourceException ex = new DuplicateResourceException("Duplicate entry");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDuplicateResourceException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("ERR_014", response.getBody().getErrorCode());
    }

    @Test
    void testHandleAttachmentProcessingException() {
        AttachmentProcessingException ex = new AttachmentProcessingException("Attachment error");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAttachmentProcessingException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ERR_015", response.getBody().getErrorCode());
    }

    @Test
    void testHandleFileProcessingException() {
        FileProcessingException ex = new FileProcessingException("File error", new RuntimeException());
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleFileProcessingException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ERR_016", response.getBody().getErrorCode());
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccessDeniedException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("ERR_017", response.getBody().getErrorCode());
    }

    @Test
    void testHandleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Bad creds");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBadCredentialsException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("ERR_018", response.getBody().getErrorCode());
    }

    @Test
    void testHandleEmailSendingException() {
        EmailSendingException ex = new EmailSendingException("Email failed to send");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleEmailSendingException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("ERR_021", response.getBody().getErrorCode());
    }

    @Test
    void testHandleAccountDisabledException() {
        AccountDisabledException ex = new AccountDisabledException("Account disabled");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccountDisabledException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("ERR_022", response.getBody().getErrorCode());
    }

    @Test
    void testHandleInvalidTokenException() {
        InvalidTokenException ex = new InvalidTokenException("Invalid token");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidTokenException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("ERR_023", response.getBody().getErrorCode());
    }

    @Test
    void testHandleGenericException() {
        java.lang.Exception ex = new java.lang.Exception("Generic error");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("ERR_999", response.getBody().getErrorCode());
    }

    @Test
    void testHandleTestingAutomationExceptionHierarchy() {
        TestingAutomationException parentEx = new TestingAutomationException("Generic custom business exception");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleTestingAutomationException(parentEx);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Generic custom business exception", response.getBody().getErrorMessage());
    }
}
