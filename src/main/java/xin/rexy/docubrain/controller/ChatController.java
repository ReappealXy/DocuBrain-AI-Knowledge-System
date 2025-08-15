package xin.rexy.docubrain.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.rexy.docubrain.common.Result;
import xin.rexy.docubrain.dto.ChatRequest;
import xin.rexy.docubrain.service.ChatService;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "智能问答模块", description = "提供基于知识库的RAG问答功能")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    @Operation(summary = "与知识库进行问答", description = "传入知识库ID和问题，获取AI生成的答案")
    public Result<String> chatWithKnowledgeBase(@RequestBody ChatRequest chatRequest) {
        // 直接调用 ChatService 完成复杂的 RAG 流程
        String aiResponse = chatService.ragChat(chatRequest);
        // 将 AI 的回答包装在 Result 对象中返回给前端
        return Result.success(aiResponse);
    }
    
    @PostMapping("/summarize/{knowledgeBaseId}")
    @Operation(summary = "总结知识库内容", description = "对指定知识库中的所有文档进行AI总结")
    public Result<String> summarizeKnowledgeBase(@PathVariable Long knowledgeBaseId) {
        String summary = chatService.summarizeKnowledgeBase(knowledgeBaseId);
        return Result.success(summary);
    }
}