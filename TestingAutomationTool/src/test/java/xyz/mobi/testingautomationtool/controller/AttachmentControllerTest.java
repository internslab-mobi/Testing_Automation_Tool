package xyz.mobi.testingautomationtool.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.service.AttachmentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AttachmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttachmentService attachmentService;

    @InjectMocks
    private AttachmentController attachmentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(attachmentController).build();
    }

    @Test
    void testUploadAttachments_Returns201Created() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "test1.txt", "text/plain", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "test2.txt", "text/plain", "content2".getBytes());

        AttachmentResponse res1 = AttachmentResponse.builder()
                .attachmentId(1)
                .attachmentType(AttachmentType.BUG)
                .bugId(10)
                .fileName("test1.txt")
                .build();

        AttachmentResponse res2 = AttachmentResponse.builder()
                .attachmentId(2)
                .attachmentType(AttachmentType.BUG)
                .bugId(10)
                .fileName("test2.txt")
                .build();

        when(attachmentService.uploadAttachments(eq(AttachmentType.BUG), eq(10), any()))
                .thenReturn(List.of(res1, res2));

        mockMvc.perform(multipart("/attachments/BUG/10")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fileName").value("test1.txt"))
                .andExpect(jsonPath("$[1].fileName").value("test2.txt"));
    }

    @Test
    void testGetAttachments_Returns200Ok() throws Exception {
        AttachmentResponse res = AttachmentResponse.builder()
                .attachmentId(1)
                .attachmentType(AttachmentType.FEATURE)
                .featureId(5)
                .fileName("spec.docx")
                .build();

        when(attachmentService.getAttachments(eq(AttachmentType.FEATURE), eq(5)))
                .thenReturn(List.of(res));

        mockMvc.perform(get("/attachments/FEATURE/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fileName").value("spec.docx"));
    }

    @Test
    void testDownloadAttachments_Returns200Ok() throws Exception {
        AttachmentDownloadResponse downloadResponse = AttachmentDownloadResponse.builder()
                .file("dummy data".getBytes())
                .fileName("project-5-attachments.zip")
                .contentType("application/zip")
                .build();

        when(attachmentService.downloadAttachments(eq(AttachmentType.PROJECT), eq(5)))
                .thenReturn(downloadResponse);

        mockMvc.perform(get("/attachments/PROJECT/5/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"project-5-attachments.zip\""))
                .andExpect(content().bytes("dummy data".getBytes()));
    }
}
