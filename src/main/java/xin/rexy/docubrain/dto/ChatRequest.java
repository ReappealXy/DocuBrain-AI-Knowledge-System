package xin.rexy.docubrain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "聊天问答请求体")
public class ChatRequest {

    @Schema(description = "目标知识库的ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long knowledgeBaseId;

    @Schema(description = "用户的提问", requiredMode = Schema.RequiredMode.REQUIRED, example = "Spring Boot 是什么？")
    private String query;

    // 可以扩展为多轮对话预留字段
    // @Schema(description = "会话ID，用于保持多轮对话上下文")
    // private String sessionId;
}