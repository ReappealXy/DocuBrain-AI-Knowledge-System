package xin.rexy.docubrain.service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xin.rexy.docubrain.dto.ChatRequest;
import xin.rexy.docubrain.service.ChatService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ChatClient chatClient;

    @Override
    public String ragChat(ChatRequest request) {
        // 检测是否为总结类查询
        String query = request.getQuery().toLowerCase();
        boolean isSummaryQuery = query.contains("总结") ||
                                query.contains("概述") ||
                                query.contains("主要讲") ||
                                query.contains("内容是什么") ||
                                query.contains("讲了什么") ||
                                query.contains("讲的是什么") ||
                                query.contains("说了什么") ||
                                query.contains("介绍");
        
        if (isSummaryQuery) {
            // 直接调用现有的summarizeKnowledgeBase方法
            return summarizeKnowledgeBase(request.getKnowledgeBaseId());
        }
        
        // 1. 从 VectorStore 中检索与用户问题相关，且属于指定知识库的上下文
        // 先尝试不使用过滤器进行搜索，调试向量搜索问题
        List<Document> allDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(request.getQuery())
                        .topK(10) // 先获取更多文档进行调试
                        .build()
        );
        
        // 手动过滤属于指定知识库的文档
        List<Document> similarDocuments = allDocuments.stream()
                .filter(doc -> {
                    Object kbId = doc.getMetadata().get("knowledgeBaseId");
                    if (kbId == null) return false;
                    
                    // 确保类型匹配：将两者都转换为Long进行比较
                    Long docKbId = null;
                    Long requestKbId = request.getKnowledgeBaseId();
                    
                    if (kbId instanceof Long) {
                        docKbId = (Long) kbId;
                    } else if (kbId instanceof String) {
                        try {
                            docKbId = Long.parseLong((String) kbId);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    } else if (kbId instanceof Integer) {
                        docKbId = ((Integer) kbId).longValue();
                    }
                    
                    return docKbId != null && docKbId.equals(requestKbId);
                })
                .limit(4)
                .toList();
        
        // 调试信息：打印搜索结果
        System.out.println("=== RAG 调试信息 ===");
        System.out.println("用户问题: " + request.getQuery());
        System.out.println("知识库ID: " + request.getKnowledgeBaseId());
        System.out.println("总搜索结果数: " + allDocuments.size());
        System.out.println("过滤后结果数: " + similarDocuments.size());
        
        for (int i = 0; i < Math.min(3, allDocuments.size()); i++) {
            Document doc = allDocuments.get(i);
            System.out.println("文档 " + (i+1) + " 元数据: " + doc.getMetadata());
            System.out.println("文档 " + (i+1) + " 内容预览: " + doc.getText().substring(0, Math.min(100, doc.getText().length())));
        }

        // 2. 将检索到的文档内容拼接成一个大的上下文文本块
        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        // 3. 构建 Prompt 模板
        String systemMessage = """
                你是一个智能知识库助手。
                你的任务是根据下面提供的“背景知识”，用清晰、简洁的语言来回答“用户的问题”。
                - 你的回答必须完全基于“背景知识”，绝对不能编造任何外部信息。
                - 如果“背景知识”中没有足够的信息来回答问题，请直接、诚实地回答：“抱歉，根据我所掌握的知识，无法回答您的问题。”
                """;

        String userMessageTemplate = """
                [背景知识]
                {context}
                
                [用户的问题]
                {query}
                """;

        // 4. 调用 ChatClient 生成最终答案
        return chatClient.prompt()
                .system(systemMessage)
                .user(userSpec -> userSpec
                        .text(userMessageTemplate)
                        .param("context", context)
                        .param("query", request.getQuery())
                )
                .call()
                .content();
    }
    
    @Override
    public String summarizeKnowledgeBase(Long knowledgeBaseId) {
        // 1. 获取知识库中的所有文档内容
        List<Document> allDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query("总结 概述 内容") // 使用通用查询词获取更多文档
                        .topK(50) // 获取更多文档用于总结
                        .build()
        );
        
        System.out.println("=== 文档总结调试信息 ===");
        System.out.println("知识库ID: " + knowledgeBaseId + " (类型: " + knowledgeBaseId.getClass().getSimpleName() + ")");
        System.out.println("向量存储中总文档数量: " + allDocuments.size());
        
        // 调试：查看前几个文档的元数据
        for (int i = 0; i < Math.min(5, allDocuments.size()); i++) {
            Document doc = allDocuments.get(i);
            Object docKbId = doc.getMetadata().get("knowledgeBaseId");
            System.out.println("文档 " + (i+1) + " 元数据: knowledgeBaseId=" + docKbId + " (类型: " + (docKbId != null ? docKbId.getClass().getSimpleName() : "null") + ")");
            System.out.println("  其他元数据: " + doc.getMetadata());
        }
        
        // 2. 过滤属于指定知识库的文档
        List<Document> knowledgeBaseDocuments = allDocuments.stream()
                .filter(doc -> {
                    Object kbId = doc.getMetadata().get("knowledgeBaseId");
                    if (kbId == null) return false;
                    
                    // 确保类型匹配：将两者都转换为Long进行比较
                    Long docKbId = null;
                    
                    if (kbId instanceof Long) {
                        docKbId = (Long) kbId;
                    } else if (kbId instanceof String) {
                        try {
                            docKbId = Long.parseLong((String) kbId);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    } else if (kbId instanceof Integer) {
                        docKbId = ((Integer) kbId).longValue();
                    }
                    
                    boolean matches = docKbId != null && docKbId.equals(knowledgeBaseId);
                    if (!matches && docKbId != null) {
                        System.out.println("过滤掉文档: docKbId=" + docKbId + ", 目标knowledgeBaseId=" + knowledgeBaseId);
                    }
                    return matches;
                })
                .toList();
        
        System.out.println("过滤后属于知识库 " + knowledgeBaseId + " 的文档数量: " + knowledgeBaseDocuments.size());
        
        if (knowledgeBaseDocuments.isEmpty()) {
            return "抱歉，该知识库中没有找到任何文档内容。";
        }
        
        // 3. 将所有文档内容合并
        String allContent = knowledgeBaseDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));
        
        // 4. 如果内容过长，进行分段总结
        if (allContent.length() > 8000) {
            return summarizeLongContent(knowledgeBaseDocuments);
        }
        
        // 5. 构建总结的 Prompt
        String systemMessage = """
                你是一个专业的知识库文档分析师，擅长提取和整理技术文档的核心信息。
                请按照以下结构化格式进行总结：
                
                ## 📋 文档概览
                - 主要主题和领域
                - 文档类型和用途
                
                ## 🎯 核心内容
                - 关键概念和定义
                - 主要知识点（用数字列表）
                
                ## 💡 重要细节
                - 技术要点或实践建议
                - 注意事项和最佳实践
                
                ## 🔗 关联信息
                - 相关技术或概念
                - 适用场景
                
                要求：
                1. 使用清晰的标题和结构化格式
                2. 突出最重要的3-5个核心要点
                3. 保持专业性和准确性
                4. 控制总结长度在500-800字
                """;
        
        String userMessage = """
                请分析以下知识库文档内容，并按照指定格式提供结构化总结：
                
                📄 **文档内容：**
                {content}
                
                请严格按照系统消息中的格式要求进行总结，确保内容准确、结构清晰、重点突出。
                """;
        
        // 6. 调用 AI 生成总结
        return chatClient.prompt()
                .system(systemMessage)
                .user(userSpec -> userSpec
                        .text(userMessage)
                        .param("content", allContent)
                )
                .call()
                .content();
    }
    
    /**
     * 对长文档进行分段总结
     */
    private String summarizeLongContent(List<Document> documents) {
        StringBuilder summaryBuilder = new StringBuilder();
        
        // 分批处理文档
        int batchSize = 5;
        for (int i = 0; i < documents.size(); i += batchSize) {
            List<Document> batch = documents.subList(i, Math.min(i + batchSize, documents.size()));
            String batchContent = batch.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n---\n"));
            
            String batchSummary = chatClient.prompt()
                    .system("你是一个专业的文档分析师，请对文档片段进行结构化总结，提取核心要点和关键信息。")
                    .user("请简洁总结以下文档片段的核心内容（控制在200字以内）：\n\n" + batchContent)
                    .call()
                    .content();
            
            summaryBuilder.append("第").append((i / batchSize) + 1).append("部分总结：\n")
                    .append(batchSummary).append("\n\n");
        }
        
        // 对所有分段总结进行最终整合
        String finalSummary = chatClient.prompt()
                .system("你是一个专业的知识库文档分析师，请将多个分段总结整合成一个完整、结构化的总结，使用清晰的标题和要点格式。")
                .user("请将以下分段总结整合成一个完整的知识库总结，保持结构化格式和逻辑连贯性：\n\n" + summaryBuilder.toString())
                .call()
                .content();
        
        return finalSummary;
    }
}