package xin.rexy.docubrain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author Rexy
 * @since 2025-08-15
 */
@Getter
@Setter
@TableName("user")
@Schema(description = "用户表")
@Data
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID",accessMode = Schema.AccessMode.READ_ONLY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "密码")
    @TableField("password")
    private String password;

    @Schema(description = "电子邮箱")
    @TableField("email")
    private String email;

    @Schema(description = "创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
