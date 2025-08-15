package xin.rexy.docubrain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import xin.rexy.docubrain.entity.DocumentChunk;
import xin.rexy.docubrain.entity.DocumentInfo;
import xin.rexy.docubrain.entity.KnowledgeBase;
import xin.rexy.docubrain.entity.User;
import xin.rexy.docubrain.mapper.KnowledgeBaseMapper;
import xin.rexy.docubrain.service.DocumentChunkService;
import xin.rexy.docubrain.service.DocumentInfoService;
import xin.rexy.docubrain.service.KnowledgeBaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 知识库表 服务实现类
 * </p>
 *
 * @author Rexy
 * @since 2025-08-15
 */
@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {

    @Autowired
    @Lazy
    private DocumentInfoService documentInfoService;
    
    @Autowired
    private DocumentChunkService documentChunkService;
    
    @Autowired
    private VectorStore vectorStore;

    @Override
    public KnowledgeBase createKnowledge(KnowledgeBase knowledgeBase, Long userId) {
        knowledgeBase.setUserId(userId);
        save(knowledgeBase);
        return knowledgeBase;
    }

    @Override
    public List<KnowledgeBase> listByUserId(Long userId) {
        LambdaQueryWrapper<KnowledgeBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeBase::getUserId, userId);
        return list(queryWrapper);
    }
    
    @Override
    @Transactional
    public void deleteKnowledgeBase(Long knowledgeBaseId, Long userId) {
        // 验证知识库是否属于当前用户
        LambdaQueryWrapper<KnowledgeBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeBase::getId, knowledgeBaseId)
                   .eq(KnowledgeBase::getUserId, userId);
        KnowledgeBase knowledgeBase = getOne(queryWrapper);
        
        if (knowledgeBase == null) {
            throw new RuntimeException("知识库不存在或无权限删除");
        }
        
        // 获取该知识库下的所有文档
        LambdaQueryWrapper<DocumentInfo> docQueryWrapper = new LambdaQueryWrapper<>();
        docQueryWrapper.eq(DocumentInfo::getKnowledgeBaseId, knowledgeBaseId);
        List<DocumentInfo> documents = documentInfoService.list(docQueryWrapper);
        
        // 删除文档分块和向量数据
        for (DocumentInfo doc : documents) {
            // 获取文档的所有分块
            LambdaQueryWrapper<DocumentChunk> chunkQueryWrapper = new LambdaQueryWrapper<>();
            chunkQueryWrapper.eq(DocumentChunk::getDocumentInfoId, doc.getId());
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
        }
        
        // 删除文档信息
        documentInfoService.remove(docQueryWrapper);
        
        // 删除知识库
        removeById(knowledgeBaseId);
        
        System.out.println("已删除知识库: " + knowledgeBase.getName() + ", ID: " + knowledgeBaseId);
    }
}
