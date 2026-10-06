package xyz.mobi.testingautomationtool.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse;
import xyz.mobi.testingautomationtool.entity.Attachment;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.AttachmentMapper;
import xyz.mobi.testingautomationtool.repository.*;
import xyz.mobi.testingautomationtool.service.impl.AttachmentServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private BugRepository bugRepository;
    @Mock
    private FeatureRepository featureRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TestCaseRepository testCaseRepository;
    @Mock
    private AuthService authService;
    @Mock
    private AttachmentMapper attachmentMapper;

    private AttachmentServiceImpl attachmentService;
    private User testUser;
    private Bug testBug;

    @BeforeEach
    void setUp() {
        attachmentService = new AttachmentServiceImpl(
                attachmentRepository,
                bugRepository,
                featureRepository,
                projectRepository,
                testCaseRepository,
                authService,
                attachmentMapper
        );

        testUser = User.builder()
                .userId(1)
                .username("tester")
                .build();

        testBug = Bug.builder()
                .bugId(1)
                .build();
    }

    @Test
    void testUploadMultipleAttachments_Success() {
        when(bugRepository.findById(1)).thenReturn(Optional.of(testBug));
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(attachmentRepository.existsByBug_BugIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(anyInt(), anyString()))
                .thenReturn(false);

        MockMultipartFile file1 = new MockMultipartFile(
                "files", "screenshot1.png", "image/png", "file1 content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "screenshot2.png", "image/png", "file2 content".getBytes());

        Attachment savedAttachment1 = Attachment.builder()
                .attachmentId(101)
                .fileName("screenshot1.png")
                .fileSize((long) "file1 content".length())
                .build();

        Attachment savedAttachment2 = Attachment.builder()
                .attachmentId(102)
                .fileName("screenshot2.png")
                .fileSize((long) "file2 content".length())
                .build();

        when(attachmentRepository.saveAll(anyList())).thenReturn(List.of(savedAttachment1, savedAttachment2));
        when(attachmentMapper.toResponse(savedAttachment1)).thenReturn(
                AttachmentResponse.builder().attachmentId(101).fileName("screenshot1.png").build());
        when(attachmentMapper.toResponse(savedAttachment2)).thenReturn(
                AttachmentResponse.builder().attachmentId(102).fileName("screenshot2.png").build());

        List<AttachmentResponse> responses = attachmentService.uploadAttachments(
                AttachmentType.BUG, 1, List.of(file1, file2));

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(attachmentRepository).saveAll(anyList());
    }

    @Test
    void testUploadAttachments_DuplicateFilenameInRequest_ThrowsException() {
        when(bugRepository.findById(1)).thenReturn(Optional.of(testBug));

        MockMultipartFile file1 = new MockMultipartFile(
                "files", "duplicate.png", "image/png", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "duplicate.png", "image/png", "content2".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                attachmentService.uploadAttachments(AttachmentType.BUG, 1, List.of(file1, file2)));

        assertTrue(ex.getMessage().contains("Duplicate filename in upload request"));
    }

    @Test
    void testUploadAttachments_DuplicateFilenameInDb_ThrowsException() {
        when(bugRepository.findById(1)).thenReturn(Optional.of(testBug));
        when(attachmentRepository.existsByBug_BugIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(1, "existing.png"))
                .thenReturn(true);

        MockMultipartFile file = new MockMultipartFile(
                "files", "existing.png", "image/png", "content".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                attachmentService.uploadAttachments(AttachmentType.BUG, 1, List.of(file)));

        assertTrue(ex.getMessage().contains("File already exists"));
    }

    @Test
    void testUploadAttachments_EmptyFile_ThrowsException() {
        when(bugRepository.findById(1)).thenReturn(Optional.of(testBug));

        MockMultipartFile emptyFile = new MockMultipartFile(
                "files", "empty.png", "image/png", new byte[0]);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                attachmentService.uploadAttachments(AttachmentType.BUG, 1, List.of(emptyFile)));

        assertTrue(ex.getMessage().contains("Uploaded file cannot be null or empty"));
    }

    @Test
    void testGetAttachments_Success() {
        when(bugRepository.existsById(1)).thenReturn(true);

        Attachment attachment = Attachment.builder()
                .attachmentId(1)
                .fileName("test.pdf")
                .fileSize(100L)
                .build();

        when(attachmentRepository.findAllByBug_BugIdAndIsDeletedFalseAndIsActiveTrue(1))
                .thenReturn(List.of(attachment));
        when(attachmentMapper.toResponse(attachment))
                .thenReturn(AttachmentResponse.builder().attachmentId(1).fileName("test.pdf").build());

        List<AttachmentResponse> result = attachmentService.getAttachments(AttachmentType.BUG, 1);

        assertEquals(1, result.size());
        assertEquals("test.pdf", result.get(0).getFileName());
    }

    @Test
    void testDownloadAttachments_SingleFile_ReturnsDirect() {
        when(bugRepository.existsById(10)).thenReturn(true);

        Attachment attachment = Attachment.builder()
                .attachmentId(1)
                .fileName("report.pdf")
                .fileType("application/pdf")
                .fileBlob("dummy-pdf-content".getBytes())
                .build();

        when(attachmentRepository.findAllByBug_BugIdAndIsDeletedFalseAndIsActiveTrue(10))
                .thenReturn(List.of(attachment));

        AttachmentDownloadResponse response = attachmentService.downloadAttachments(AttachmentType.BUG, 10);

        assertNotNull(response);
        assertEquals("report.pdf", response.getFileName());
        assertEquals("application/pdf", response.getContentType());
        assertArrayEquals("dummy-pdf-content".getBytes(), response.getFile());
    }

    @Test
    void testDownloadAttachments_MultipleFiles_ReturnsZip() throws IOException {
        when(bugRepository.existsById(20)).thenReturn(true);

        Attachment a1 = Attachment.builder()
                .attachmentId(1)
                .fileName("file1.txt")
                .fileType("text/plain")
                .fileBlob("hello world".getBytes())
                .build();

        Attachment a2 = Attachment.builder()
                .attachmentId(2)
                .fileName("file2.txt")
                .fileType("text/plain")
                .fileBlob("goodbye world".getBytes())
                .build();

        when(attachmentRepository.findAllByBug_BugIdAndIsDeletedFalseAndIsActiveTrue(20))
                .thenReturn(List.of(a1, a2));

        AttachmentDownloadResponse response = attachmentService.downloadAttachments(AttachmentType.BUG, 20);

        assertNotNull(response);
        assertEquals("bug-20-attachments.zip", response.getFileName());
        assertEquals("application/zip", response.getContentType());
        assertTrue(response.getFile().length > 0);

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(response.getFile()))) {
            assertNotNull(zis.getNextEntry());
            assertNotNull(zis.getNextEntry());
            assertNull(zis.getNextEntry());
        }
    }

    @Test
    void testDownloadAttachments_NoAttachments_ThrowsResourceNotFound() {
        when(bugRepository.existsById(30)).thenReturn(true);
        when(attachmentRepository.findAllByBug_BugIdAndIsDeletedFalseAndIsActiveTrue(30))
                .thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () ->
                attachmentService.downloadAttachments(AttachmentType.BUG, 30));
    }
}
