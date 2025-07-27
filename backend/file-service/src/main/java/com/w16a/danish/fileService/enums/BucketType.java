package com.w16a.danish.fileService.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * Enum representing different types of buckets in the file storage service.
 *
 * @author Eddy ZHANG
 * @date 2025/03/28
 */
@Getter
@AllArgsConstructor
public enum BucketType {
    USER_AVATAR("user-avatar", true),
    COMPETITION_ASSETS("competition-assets", true),
    SUBMISSIONS("submissions", true);

    private final String bucketName;
    private final boolean publicRead;
}
