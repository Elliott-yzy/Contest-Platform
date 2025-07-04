package com.w16a.danish.fileService.controller;

import com.w16a.danish.fileService.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * File Upload Controller
 *
 * @author Eddy ZHANG
 * @date 2025/03/27
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAvatar(@RequestPart("file") MultipartFile file) {
        String uploadedUrl = fileStorageService.uploadAvatar(file);
        return ResponseEntity.ok(uploadedUrl);
    }

    @PostMapping(value = "/upload/promo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCompetitionPromo(@RequestPart("file") MultipartFile file) {
        String uploadedUrl = fileStorageService.uploadCompetitionPromo(file);
        return ResponseEntity.ok(uploadedUrl);
    }

    @PostMapping(value = "/upload/submission", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadSubmission(@RequestPart("file") MultipartFile file) {
        String objectName = fileStorageService.uploadSubmission(file);
        return ResponseEntity.ok(objectName);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam("bucket") String bucket,
                                             @RequestParam("objectName") String objectName) {
        fileStorageService.deleteFile(bucket, objectName);
        return ResponseEntity.ok("File deleted successfully.");
    }

}
