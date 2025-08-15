package xin.rexy.docubrain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import xin.rexy.docubrain.controller.UserController;
import xin.rexy.docubrain.entity.User;
import xin.rexy.docubrain.mapper.UserMapper;
import xin.rexy.docubrain.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Rexy
 * @since 2025-08-15
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    public boolean register(String username, String password, String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);

        if (this.count(queryWrapper) > 0) {
            System.out.println("注册失败：用户名 '" + username + "' 已被占用。");
            return false;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);
        boolean success = this.save(newUser);
        if (success) {
            System.out.println("用户 '" + username + "' 注册成功！");
        } else {
            System.out.println("注册失败：数据库插入时发生未知错误。");
        }
        return success;
    }

    @Override
    public User login(String username, String password,String  email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getUsername, username);
        User user=this.getOne(queryWrapper);
        if (user == null|| !user.getPassword().equals(password)) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return user;
    }
}
