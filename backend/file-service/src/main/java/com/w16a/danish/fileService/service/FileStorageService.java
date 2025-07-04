package com.w16a.danish.fileService.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for handling file storage operations with MinIO.
 * Provides methods for uploading user avatars, competition promotional assets,
 * and participant submissions, as well as file deletion and secure access.
 *
 * @author Eddy ZHANG
 * @date 2025/03/27
 */
public interface FileStorageService {

    /**
     * Uploads a user avatar to a public MinIO bucket.
     *
     * @param file the avatar image file to upload
     * @return the public URL of the uploaded avatar
     */
    String uploadAvatar(MultipartFile file);

    /**
     * Uploads a competition promotional video or material to a public bucket.
     *
     * @param file the promotional file to upload
     * @return the public URL of the uploaded promo file
     */
    String uploadCompetitionPromo(MultipartFile file);

    /**
     * Uploads a participant's submission (e.g., PDF, video) to a private bucket.
     *
     * @param file the submission file
     * @return the internal object name in the bucket (not a URL)
     */
    String uploadSubmission(MultipartFile file);

    /**
     * Deletes a file from the specified bucket.
     *
     * @param bucketName  the name of the bucket
     * @param objectName  the name of the file/object to delete
     */
    void deleteFile(String bucketName, String objectName);

}
