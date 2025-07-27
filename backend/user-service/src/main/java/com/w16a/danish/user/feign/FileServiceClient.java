package com.w16a.danish.user.feign;

import com.w16a.danish.user.config.FeignMultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * Feign client for file service.
 *
 * @author Eddy ZHANG
 * @date 2025/03/28
 */
@FeignClient(name = "file-service", path = "/files", configuration = FeignMultipartSupportConfig.class)
public interface FileServiceClient {

    @PostMapping(value = "/upload/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> uploadAvatar(@RequestPart("file") MultipartFile file);

    @DeleteMapping("/delete")
    ResponseEntity<String> deleteFile(@RequestParam("bucket") String bucket,
                                      @RequestParam("objectName") String objectName);
}
