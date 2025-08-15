package xin.rexy.docubrain.mapper;

import xin.rexy.docubrain.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author Rexy
 * @since 2025-08-15
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
