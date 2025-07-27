package com.w16a.danish.user.domain.po;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * @author Eddy ZHANG
 * @date 2025/03/16
 * @description User Table
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("users")
@Schema(name = "Users", description = "User Table (Stores user account details)")
public class Users implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "User ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "User's full name", example = "John Doe")
    private String name;

    @Schema(description = "Email address (unique)", example = "johndoe@example.com")
    private String email;

    @Schema(description = "Hashed password (bcrypt)", example = "$2a$10$7qJlMXXsbbBW8MgFjU1W1Okkx9")
    private String password;

    @Schema(description = "Short bio or description of the user", example = "Software Engineer at ABC Corp.", nullable = true)
    private String description;

    @Schema(description = "URL to the user's avatar image", example = "https://example.com/avatar.jpg", nullable = true)
    private String avatarUrl;

    @Schema(description = "Timestamp when the user was created", example = "2025-03-16T12:00:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the user was last updated", example = "2025-03-17T14:30:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;


}
