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
 * 文档信息表
 * </p>
 *
 * @author Rexy
 * @since 2025-08-15
 */
@Getter
@Setter
@TableName("document_info")
@Schema(description = "文档信息表")
public class DocumentInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="主键ID",accessMode = Schema.AccessMode.READ_ONLY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description ="所属知识库ID",requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("knowledge_base_id")
    private Long knowledgeBaseId;

    @Schema(description ="原始文件名",accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("file_name")
    private String fileName;

    @Schema(description ="文件类型 (e.g., pdf, txt)",accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("file_type")
    private String fileType;

    @Schema(description ="文件大小 (字节)",accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("file_size")
    private Long fileSize;

    @Schema(description ="处理状态 (PENDING, INDEXING, COMPLETED, FAILED)",accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("status")
    private String status;

    @Schema(description ="上传时间",accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description ="更新时间",accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
