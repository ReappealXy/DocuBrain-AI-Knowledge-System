package xin.rexy.docubrain.service;

import xin.rexy.docubrain.dto.ChatRequest;

public interface ChatService {

    /**
     * 执行一次完整的 RAG 问答流程
     * @param request 包含知识库ID和用户问题的请求对象
     * @return AI 生成的最终回答
     */
    String ragChat(ChatRequest request);
    
    /**
     * 对指定知识库进行文档总结
     * @param knowledgeBaseId 知识库ID
     * @return AI 生成的知识库总结
     */
    String summarizeKnowledgeBase(Long knowledgeBaseId);
}