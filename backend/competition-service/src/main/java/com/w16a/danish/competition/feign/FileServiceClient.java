package com.w16a.danish.competition.feign;

import com.w16a.danish.competition.config.FeignMultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
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

    @PostMapping(value = "/upload/promo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> uploadCompetitionPromo(@RequestPart("file") MultipartFile file);

    @DeleteMapping("/delete")
    ResponseEntity<String> deleteFile(@RequestParam("bucket") String bucket,
                                      @RequestParam("objectName") String objectName);
}
