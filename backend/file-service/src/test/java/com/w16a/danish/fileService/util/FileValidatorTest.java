package com.w16a.danish.fileService.util;

import com.w16a.danish.fileService.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Unit tests for {@link FileValidator} utility class.
 */
class FileValidatorTest {

    @Test
    @DisplayName("✅ Should pass validation for valid image file")
    void testValidateImage_Success() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "dummy content".getBytes()
        );

        assertDoesNotThrow(() -> FileValidator.validateImage(file));
    }

    @Test
    @DisplayName("❌ Should throw for invalid image file type")
    void testValidateImage_InvalidType() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "dummy content".getBytes()
        );

        assertThatThrownBy(() -> FileValidator.validateImage(file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid image type");
    }

    @Test
    @DisplayName("✅ Should pass validation for valid basic file")
    void testValidateBasic_Success() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                "dummy content".getBytes()
        );

        assertDoesNotThrow(() -> FileValidator.validateBasic(file));
    }

    @Test
    @DisplayName("❌ Should throw when basic file is empty")
    void testValidateBasic_EmptyFile() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        assertThatThrownBy(() -> FileValidator.validateBasic(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("File is empty");
    }
}
