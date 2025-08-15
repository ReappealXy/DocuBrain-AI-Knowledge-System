package xin.rexy.docubrain.mapper;

import xin.rexy.docubrain.entity.DocumentInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 文档信息表 Mapper 接口
 * </p>
 *
 * @author Rexy
 * @since 2025-08-15
 */
@Mapper
public interface DocumentInfoMapper extends BaseMapper<DocumentInfo> {

    /**
     * 统计用户所有知识库中的文档总数
     * @param userId 用户ID
     * @return 文档总数
     */
    @Select("SELECT COUNT(*) FROM document_info di " +
            "INNER JOIN knowledge_base kb ON di.knowledge_base_id = kb.id " +
            "WHERE kb.user_id = #{userId} AND di.status = 'COMPLETED'")
    long countDocumentsByUserId(@Param("userId") Long userId);

}
