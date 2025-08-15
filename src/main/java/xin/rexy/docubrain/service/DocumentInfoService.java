package xin.rexy.docubrain.service;

import org.springframework.web.multipart.MultipartFile;
import xin.rexy.docubrain.entity.DocumentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 文档信息表 服务类
 * </p>
 *
 * @author Rexy
 * @since 2025-08-15
 */
public interface DocumentInfoService extends IService<DocumentInfo> {

    void uploadAndIndex(MultipartFile file, Long knowledgeBaseId, Long userId);
    
    /**
     * 统计指定知识库中的文档数量
     * @param knowledgeBaseId 知识库ID
     * @return 文档数量
     */
    long countByKnowledgeBaseId(Long knowledgeBaseId);
    
    /**
     * 统计用户所有知识库中的文档总数
     * @param userId 用户ID
     * @return 文档总数
     */
    long countByUserId(Long userId);
    
    /**
     * 删除文档及其相关的向量数据
     * @param documentId 文档ID
     * @param userId 用户ID
     */
    void deleteDocument(Long documentId, Long userId);
    
    /**
     * 重新处理PENDING状态的文档
     * @param knowledgeBaseId 知识库ID
     * @param userId 用户ID
     */
    void reprocessPendingDocuments(Long knowledgeBaseId, Long userId);
}
