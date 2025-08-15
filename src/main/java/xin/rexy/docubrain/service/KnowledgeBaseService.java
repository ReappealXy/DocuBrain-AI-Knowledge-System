package xin.rexy.docubrain.service;

import xin.rexy.docubrain.entity.KnowledgeBase;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 知识库表 服务类
 * </p>
 *
 * @author Rexy
 * @since 2025-08-15
 */
public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    KnowledgeBase createKnowledge(KnowledgeBase knowledgeBase, Long UserId);

    List<KnowledgeBase> listByUserId(Long userId);
    
    void deleteKnowledgeBase(Long knowledgeBaseId, Long userId);
}
