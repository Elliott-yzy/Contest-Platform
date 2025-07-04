package com.w16a.danish.fileService.controller;

import com.w16a.danish.fileService.service.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link FileUploadController}.
 * Covers upload avatar, upload promo, upload submission, and delete file APIs.
 */
@WebMvcTest(FileUploadController.class)
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("✅ Upload avatar successfully")
    void testUploadAvatarSuccess() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, "fake image content".getBytes()
        );

        Mockito.when(fileStorageService.uploadAvatar(any(MultipartFile.class)))
                .thenReturn("http://mocked-url/avatar.png");

        mockMvc.perform(multipart("/files/upload/avatar")
                        .file(mockFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("http://mocked-url/avatar.png"));
    }

    @Test
    @DisplayName("✅ Upload competition promo successfully")
    void testUploadCompetitionPromoSuccess() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "promo.mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE, "fake video content".getBytes()
        );

        Mockito.when(fileStorageService.uploadCompetitionPromo(any(MultipartFile.class)))
                .thenReturn("http://mocked-url/promo.mp4");

        mockMvc.perform(multipart("/files/upload/promo")
                        .file(mockFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("http://mocked-url/promo.mp4"));
    }

    @Test
    @DisplayName("✅ Upload submission successfully")
    void testUploadSubmissionSuccess() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "submission.pdf", MediaType.APPLICATION_PDF_VALUE, "fake pdf content".getBytes()
        );

        Mockito.when(fileStorageService.uploadSubmission(any(MultipartFile.class)))
                .thenReturn("submission-folder/submission.pdf");

        mockMvc.perform(multipart("/files/upload/submission")
                        .file(mockFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("submission-folder/submission.pdf"));
    }

    @Test
    @DisplayName("✅ Delete file successfully")
    void testDeleteFileSuccess() throws Exception {
        Mockito.doNothing().when(fileStorageService).deleteFile(anyString(), anyString());

        mockMvc.perform(delete("/files/delete")
                        .param("bucket", "test-bucket")
                        .param("objectName", "test-file.png"))
                .andExpect(status().isOk())
                .andExpect(content().string("File deleted successfully."));
    }
}
