package com.w16a.danish.fileService.service.impl;

import cn.hutool.core.lang.UUID;
import com.w16a.danish.fileService.config.MinioPropertiesConfig;
import com.w16a.danish.fileService.enums.BucketType;
import com.w16a.danish.fileService.exception.BusinessException;
import com.w16a.danish.fileService.service.FileStorageService;
import com.w16a.danish.fileService.util.FileValidator;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


/**
 * Implementation of the FileStorageService interface for interacting with MinIO object storage.
 * Provides functionality to upload files to different buckets (avatars, promos, submissions),
 * generate temporary access URLs, and delete files.
 *
 * @author Eddy ZHANG
 * @date 2025/03/28
 */
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;
    private final MinioPropertiesConfig minioPropertiesConfig;

    /**
     * Uploads user avatar image to the public avatar bucket.
     */
    @Override
    public String uploadAvatar(MultipartFile file) {
        FileValidator.validateImage(file);
        return upload(BucketType.USER_AVATAR, file);
    }

    /**
     * Uploads competition promo assets (videos/images) to the public promo bucket.
     */
    @Override
    public String uploadCompetitionPromo(MultipartFile file) {
        FileValidator.validateBasic(file);
        return upload(BucketType.COMPETITION_ASSETS, file);
    }


    /**
     * Uploads a participant's submission to the private submission bucket.
     */
    @Override
    public String uploadSubmission(MultipartFile file) {
        FileValidator.validateBasic(file);
        return upload(BucketType.SUBMISSIONS, file);
    }

    /**
     * Common logic for uploading a file to a given bucket type.
     * Returns public URL if the bucket is public, else returns the object name.
     */
    private String upload(BucketType bucketType, MultipartFile file) {
        try {
            ensureBucketExists(bucketType);
            String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketType.getBucketName())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            String publicEndpoint = minioPropertiesConfig.getPublicEndpoint();
            if (publicEndpoint.endsWith("/")) {
                publicEndpoint = publicEndpoint.substring(0, publicEndpoint.length() - 1);
            }

            return publicEndpoint + "/" + bucketType.getBucketName() + "/" + objectName;
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed: " + e.getMessage());
        }
    }

    /**
     * Ensures the bucket exists; creates it if not found.
     * Applies a public-read bucket policy by default.
     */
    private void ensureBucketExists(BucketType bucketType) throws Exception {
        String bucketName = bucketType.getBucketName();
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

            // Define public read policy for the bucket
            String policy = "{\n" +
                    "  \"Version\": \"2012-10-17\",\n" +
                    "  \"Statement\": [\n" +
                    "    {\n" +
                    "      \"Sid\": \"PublicRead\",\n" +
                    "      \"Effect\": \"Allow\",\n" +
                    "      \"Principal\": \"*\",\n" +
                    "      \"Action\": [\n" +
                    "        \"s3:GetObject\"\n" +
                    "      ],\n" +
                    "      \"Resource\": [\n" +
                    "        \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            // Apply bucket policy
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policy)
                    .build());
        }
    }

    /**
     * Deletes a file from the specified bucket if it exists.
     */
    @Override
    public void deleteFile(String bucketName, String objectName) {
        try {
            // Check if the object exists in the bucket
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            // Remove the object from the bucket
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

        } catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "File deletion failed: " + e.getMessage());
        }
    }

}
