package xin.rexy.docubrain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xin.rexy.docubrain.entity.DocumentChunk;
import xin.rexy.docubrain.entity.DocumentInfo;
import xin.rexy.docubrain.entity.KnowledgeBase;
import xin.rexy.docubrain.mapper.DocumentInfoMapper;
import xin.rexy.docubrain.service.DocumentChunkService;
import xin.rexy.docubrain.service.DocumentInfoService;
import xin.rexy.docubrain.service.KnowledgeBaseService;

import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 文档信息表 服务实现类
 * </p>
 *
 * @author Rexy Xin
 * @since 2025-08-15
 */
@Service
public class DocumentInfoServiceImpl extends ServiceImpl<DocumentInfoMapper, DocumentInfo> implements DocumentInfoService {

    private final VectorStore vectorStore;
    private final DocumentChunkService documentChunkService;
    private final KnowledgeBaseService knowledgeBaseService;

    // 使用构造器注入，这是 Spring 推荐的最佳实践
    @Autowired
    public DocumentInfoServiceImpl(VectorStore vectorStore, DocumentChunkService documentChunkService, KnowledgeBaseService knowledgeBaseService) {
        this.vectorStore = vectorStore;
        this.documentChunkService = documentChunkService;
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /**
     * 核心方法：接收上传的文件，进行解析、分块、向量化，并将元数据和向量数据分别存入数据库。
     * 使用 @Transactional 注解确保整个过程的原子性。
     *
     * @param file              用户上传的文件
     * @param knowledgeBaseId   文件所属的知识库ID
     * @param userId            执行操作的用户ID (当前版本暂未使用，为后续扩展保留)
     */
    @Override
    @Transactional
    public void uploadAndIndex(MultipartFile file, Long knowledgeBaseId, Long userId) {
        System.out.println("=== 文档上传开始 ===");
        System.out.println("文件名: " + file.getOriginalFilename());
        System.out.println("知识库ID: " + knowledgeBaseId);
        System.out.println("用户ID: " + userId);
        
        // 1. 在数据库中创建文档记录，初始状态为"索引中"
        DocumentInfo docInfo = new DocumentInfo();
        docInfo.setKnowledgeBaseId(knowledgeBaseId);
        docInfo.setFileName(file.getOriginalFilename());
        docInfo.setFileType(file.getContentType());
        docInfo.setFileSize(file.getSize());
        docInfo.setStatus("INDEXING");
        this.save(docInfo); // 保存到 document_info 表，ID会自动回填
        
        System.out.println("文档记录已保存，ID: " + docInfo.getId());
        System.out.println("保存的知识库ID: " + docInfo.getKnowledgeBaseId());

        try {
            // 2. 使用 Tika 解析文件内容
            // 将 MultipartFile 转换为 Spring 的 ByteArrayResource，避免临时文件问题
            ByteArrayResource resource = new ByteArrayResource(file.getBytes());
            TikaDocumentReader tikaReader = new TikaDocumentReader(resource);
            List<Document> rawDocuments = tikaReader.get();

            // 3. 使用 Spring AI 的 TokenTextSplitter 进行智能文本分块
            // 使用默认构造器，这样可以获得更多的分块
            TokenTextSplitter textSplitter = new TokenTextSplitter();
            List<Document> chunks = textSplitter.apply(rawDocuments);
            
            System.out.println("=== 文档处理调试信息 ===");
            System.out.println("原始文档数量: " + rawDocuments.size());
            System.out.println("分块后数量: " + chunks.size());
            System.out.println("知识库ID: " + knowledgeBaseId);

            // 4. 为每个分块添加关键的元数据
            for (int i = 0; i < chunks.size(); i++) {
                Document chunk = chunks.get(i);
                Map<String, Object> metadata = chunk.getMetadata();
                // 这些元数据将存入 Redis，用于 RAG 流程中的精确筛选
                metadata.put("knowledgeBaseId", knowledgeBaseId); // 保持 Long 类型
                metadata.put("documentInfoId", docInfo.getId());
                metadata.put("fileName", file.getOriginalFilename());
                metadata.put("chunkOrder", i + 1); // 顺序从 1 开始
                
                // 调试信息：验证元数据设置
                if (i == 0) {
                    System.out.println("第一个分块的元数据: " + metadata);
                    System.out.println("knowledgeBaseId类型: " + knowledgeBaseId.getClass().getSimpleName());
                }
            }

            // 5. 将分块批量存入 VectorStore (Redis)
            // 这一步会自动调用 OpenAI Embedding API 为每个块生成向量
            vectorStore.add(chunks);

            // 6. 将分块的元数据批量保存到我们的 MySQL 数据库中
            List<DocumentChunk> chunkEntities = new ArrayList<>();
            for (Document chunk : chunks) {
                DocumentChunk chunkEntity = new DocumentChunk();
                chunkEntity.setDocumentInfoId(docInfo.getId());
                chunkEntity.setVectorId(chunk.getId()); // VectorStore 会为每个 chunk 生成一个唯一的 vectorId
                chunkEntity.setChunkText(chunk.getText());
                chunkEntity.setChunkOrder((Integer) chunk.getMetadata().get("chunkOrder"));
                chunkEntities.add(chunkEntity);
            }
            documentChunkService.saveBatch(chunkEntities); // 使用批量插入，提高性能

            // 7. 所有步骤成功，更新文档最终状态为"完成"
            docInfo.setStatus("COMPLETED");
            this.updateById(docInfo);
            
            System.out.println("=== 文档上传完成 ===");
            System.out.println("文档ID: " + docInfo.getId() + " 状态已更新为: COMPLETED");
            System.out.println("最终知识库ID: " + docInfo.getKnowledgeBaseId());

        } catch (Exception e) {
            // 8. 如果过程中任何一步发生异常
            e.printStackTrace(); // 在服务器日志中打印完整的错误堆栈，方便排查

            // 更新数据库中的文档状态为“失败”
            docInfo.setStatus("FAILED");
            this.updateById(docInfo);

            // 向上抛出运行时异常。
            // 因为方法被 @Transactional 注解，这个异常会触发整个事务的回滚。
            // 这意味着 `document_info` 表中的记录和任何已保存的 `document_chunk` 记录都会被删除，保证数据一致性。
            throw new RuntimeException("文件处理和索引失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public long countByKnowledgeBaseId(Long knowledgeBaseId) {
        System.out.println("=== 统计知识库文档数量 ===");
        System.out.println("查询知识库ID: " + knowledgeBaseId);
        
        LambdaQueryWrapper<DocumentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocumentInfo::getKnowledgeBaseId, knowledgeBaseId)
                   .eq(DocumentInfo::getStatus, "COMPLETED"); // 只统计成功上传的文档
        
        long count = count(queryWrapper);
        System.out.println("知识库 " + knowledgeBaseId + " 的文档数量: " + count);
        
        // 额外调试：查看该知识库的所有文档（包括未完成的）
        LambdaQueryWrapper<DocumentInfo> allDocsWrapper = new LambdaQueryWrapper<>();
        allDocsWrapper.eq(DocumentInfo::getKnowledgeBaseId, knowledgeBaseId);
        List<DocumentInfo> allDocs = list(allDocsWrapper);
        System.out.println("知识库 " + knowledgeBaseId + " 的所有文档（含未完成）: " + allDocs.size());
        for (DocumentInfo doc : allDocs) {
            System.out.println("  - 文档ID: " + doc.getId() + ", 文件名: " + doc.getFileName() + ", 状态: " + doc.getStatus() + ", 知识库ID: " + doc.getKnowledgeBaseId());
        }
        
        return count;
    }
    
    @Override
    public long countByUserId(Long userId) {
        // 通过关联查询统计用户所有知识库中的文档数量
        return baseMapper.countDocumentsByUserId(userId);
    }
    
    @Override
    @Transactional
    public void deleteDocument(Long documentId, Long userId) {
        // 验证文档是否属于当前用户
        DocumentInfo document = getById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }
        
        // 验证知识库是否属于当前用户
        LambdaQueryWrapper<KnowledgeBase> kbQueryWrapper = new LambdaQueryWrapper<>();
        kbQueryWrapper.eq(KnowledgeBase::getId, document.getKnowledgeBaseId())
                     .eq(KnowledgeBase::getUserId, userId);
        KnowledgeBase knowledgeBase = knowledgeBaseService.getOne(kbQueryWrapper);
        if (knowledgeBase == null) {
            throw new RuntimeException("无权限删除此文档");
        }
        
        // 获取文档的所有分块
        LambdaQueryWrapper<DocumentChunk> chunkQueryWrapper = new LambdaQueryWrapper<>();
        chunkQueryWrapper.eq(DocumentChunk::getDocumentInfoId, documentId);
        List<DocumentChunk> chunks = documentChunkService.list(chunkQueryWrapper);
        
        // 从向量存储中删除分块
        List<String> chunkIds = chunks.stream()
                .map(chunk -> chunk.getVectorId())
                .collect(Collectors.toList());
        
        if (!chunkIds.isEmpty()) {
            vectorStore.delete(chunkIds);
        }
        
        // 删除数据库中的分块记录
        documentChunkService.remove(chunkQueryWrapper);
        
        // 删除文档信息
        removeById(documentId);
        
        System.out.println("已删除文档: " + document.getFileName() + ", ID: " + documentId);
    }
    
    @Override
    @Transactional
    public void reprocessPendingDocuments(Long knowledgeBaseId, Long userId) {
        System.out.println("=== 重新处理PENDING文档开始 ===");
        System.out.println("知识库ID: " + knowledgeBaseId);
        System.out.println("用户ID: " + userId);
        
        // 验证知识库是否属于当前用户
        LambdaQueryWrapper<KnowledgeBase> kbQueryWrapper = new LambdaQueryWrapper<>();
        kbQueryWrapper.eq(KnowledgeBase::getId, knowledgeBaseId)
                     .eq(KnowledgeBase::getUserId, userId);
        KnowledgeBase knowledgeBase = knowledgeBaseService.getOne(kbQueryWrapper);
        if (knowledgeBase == null) {
            throw new RuntimeException("知识库不存在或无权限访问");
        }
        
        // 获取该知识库下所有PENDING状态的文档
        LambdaQueryWrapper<DocumentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocumentInfo::getKnowledgeBaseId, knowledgeBaseId)
                   .eq(DocumentInfo::getStatus, "PENDING");
        List<DocumentInfo> pendingDocs = list(queryWrapper);
        
        System.out.println("找到 " + pendingDocs.size() + " 个PENDING状态的文档");
        
        for (DocumentInfo doc : pendingDocs) {
            try {
                System.out.println("重新处理文档: " + doc.getFileName() + ", ID: " + doc.getId());
                
                // 更新状态为INDEXING
                doc.setStatus("INDEXING");
                updateById(doc);
                
                // 删除旧的分块数据（如果存在）
                LambdaQueryWrapper<DocumentChunk> chunkQueryWrapper = new LambdaQueryWrapper<>();
                chunkQueryWrapper.eq(DocumentChunk::getDocumentInfoId, doc.getId());
                List<DocumentChunk> oldChunks = documentChunkService.list(chunkQueryWrapper);
                
                if (!oldChunks.isEmpty()) {
                    // 从向量存储中删除旧分块
                    List<String> chunkIds = oldChunks.stream()
                            .map(DocumentChunk::getVectorId)
                            .collect(Collectors.toList());
                    vectorStore.delete(chunkIds);
                    
                    // 删除数据库中的旧分块记录
                    documentChunkService.remove(chunkQueryWrapper);
                    System.out.println("已删除 " + oldChunks.size() + " 个旧分块");
                }
                
                // 模拟文档内容（实际应用中需要从文件系统或其他地方获取原始文件）
                // 这里我们创建一个简单的文档内容用于测试
                String simulatedContent = "这是文档 " + doc.getFileName() + " 的模拟内容。\n" +
                        "知识库ID: " + knowledgeBaseId + "\n" +
                        "文档ID: " + doc.getId() + "\n" +
                        "这个文档现在应该正确关联到知识库 " + knowledgeBase.getName() + "。";
                
                // 创建文档对象
                List<Document> rawDocuments = List.of(new Document(simulatedContent));
                
                // 使用 Spring AI 的 TokenTextSplitter 进行智能文本分块
                TokenTextSplitter textSplitter = new TokenTextSplitter();
                List<Document> chunks = textSplitter.apply(rawDocuments);
                
                System.out.println("文档分块数量: " + chunks.size());
                
                // 为每个分块添加关键的元数据
                for (int i = 0; i < chunks.size(); i++) {
                    Document chunk = chunks.get(i);
                    Map<String, Object> metadata = chunk.getMetadata();
                    // 这些元数据将存入 Redis，用于 RAG 流程中的精确筛选
                    metadata.put("knowledgeBaseId", knowledgeBaseId); // 保持 Long 类型
                    metadata.put("documentInfoId", doc.getId());
                    metadata.put("fileName", doc.getFileName());
                    metadata.put("chunkOrder", i + 1); // 顺序从 1 开始
                    
                    // 调试信息：验证元数据设置
                    if (i == 0) {
                        System.out.println("重新处理 - 第一个分块的元数据: " + metadata);
                        System.out.println("重新处理 - knowledgeBaseId类型: " + knowledgeBaseId.getClass().getSimpleName());
                    }
                }
                
                // 将分块批量存入 VectorStore (Redis)
                vectorStore.add(chunks);
                
                // 将分块的元数据批量保存到我们的 MySQL 数据库中
                List<DocumentChunk> chunkEntities = new ArrayList<>();
                for (Document chunk : chunks) {
                    DocumentChunk chunkEntity = new DocumentChunk();
                    chunkEntity.setDocumentInfoId(doc.getId());
                    chunkEntity.setVectorId(chunk.getId());
                    chunkEntity.setChunkText(chunk.getText());
                    chunkEntity.setChunkOrder((Integer) chunk.getMetadata().get("chunkOrder"));
                    chunkEntities.add(chunkEntity);
                }
                documentChunkService.saveBatch(chunkEntities);
                
                // 更新文档状态为完成
                doc.setStatus("COMPLETED");
                updateById(doc);
                
                System.out.println("文档重新处理完成: " + doc.getFileName());
                
            } catch (Exception e) {
                System.err.println("重新处理文档失败: " + doc.getFileName() + ", 错误: " + e.getMessage());
                e.printStackTrace();
                
                // 更新状态为失败
                doc.setStatus("FAILED");
                updateById(doc);
            }
        }
        
        System.out.println("=== 重新处理PENDING文档完成 ===");
    }
}