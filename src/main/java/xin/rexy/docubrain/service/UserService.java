package xin.rexy.docubrain.service;

import xin.rexy.docubrain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author Rexy
 * @since 2025-08-15
 */
public interface UserService extends IService<User> {

    boolean register(String username, String password, String email);

    User login(String username, String password, String email);
}
