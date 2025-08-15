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
 * 文档分块表
 * </p>
 *
 * @author Rexy
 * @since 2025-08-15
 */
@Getter
@Setter
@TableName("document_chunk")
@Schema(description = "文档分块表")
public class DocumentChunk implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "所属文档ID",accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("document_info_id")
    private Long documentInfoId;

    @Schema(description = "在向量数据库中的唯一ID",accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("vector_id")
    private String vectorId;

    @Schema(description = "文本块原文",accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("chunk_text")
    private String chunkText;

    @Schema(description = "文本块在原文中的顺序",accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("chunk_order")
    private Integer chunkOrder;

    @Schema(description = "创建时间",accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("created_at")
    private LocalDateTime createdAt;
}
