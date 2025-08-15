package xin.rexy.docubrain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 知识库表
 * </p>
 *
 * @author Rexy
 * @since 2025-08-15
 */
@Getter
@Setter
@TableName("knowledge_base")
@Schema(description = "知识库表")
public class KnowledgeBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID", accessMode = Schema.AccessMode.READ_ONLY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "所属用户ID", accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("user_id")
    private Long userId;

    @Schema(description = "知识库名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("name")
    private String name;

    @Schema(description = "知识库描述")
    @TableField("description")
    private String description;

    @Schema(description = "创建时间",accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间",accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
