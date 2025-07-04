package com.w16a.danish.fileService.util;

import com.w16a.danish.fileService.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * File Validator Utility
 *
 * @author Eddy ZHANG
 * @date 2025/03/28
 */
public class FileValidator {

    public static void validateImage(MultipartFile file) {
        validateBasic(file);
        // Validate image content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid image type: " + contentType);
        }

        // Validate image file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif)$")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid image file: " + filename);
        }
    }


    public static void validateBasic(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "File is empty.");
        }
    }
}

