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
 * @description User-Role Mapping Table
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_roles")
@Schema(name = "UserRoles", description = "User-Role Mapping Table")
public class UserRoles implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "User ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
    @TableId(value = "user_id", type = IdType.ASSIGN_UUID)
    private String userId;

    @Schema(description = "Role ID (Foreign Key, references roles table)", example = "1")
    private Integer roleId;

    @Schema(description = "Timestamp when role was assigned", example = "2025-03-16T12:00:00")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;


}
