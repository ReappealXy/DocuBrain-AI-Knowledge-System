package xin.rexy.docubrain.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.rexy.docubrain.common.Result;
import xin.rexy.docubrain.common.SecurityUtils;
import xin.rexy.docubrain.entity.User;
import xin.rexy.docubrain.service.UserService;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author Rexy Xin
 * @since 2025-08-15
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户认证模块", description = "提供用户注册和登录接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     * @param user 包含用户名和密码的用户注册信息
     * @return 包含成功消息和用户ID的Result
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "传入用户名、密码和邮箱进行新用户注册。")
    public Result<?> register(@RequestBody User user) {
        // 直接调用 Service 方法，不再需要 try-catch
        // 如果 userService.register 抛出异常，会被 GlobalExceptionHandler 捕获
        boolean register = userService.register(user.getUsername(), user.getPassword(),user.getEmail());
        if (!register) {
            return Result.error("用户已存在！");
        }
        return Result.success("注册成功！");
    }

    /**
     * 用户登录接口
     * @param user 包含用户名和密码的登录凭证
     * @return 包含成功消息和用户信息的Result
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "传入用户名、密码和邮箱进行登录认证。")
    public Result<User> login(@RequestBody User user) {
        // 直接调用 Service 方法
        // 如果用户名或密码错误，userService.login 会抛出异常，并被 GlobalExceptionHandler 捕获
        User loggedInUser = userService.login(user.getUsername(), user.getPassword(), null);
        
        // 登录成功后，将用户ID设置到Session中
        SecurityUtils.setUserId(loggedInUser.getId());
        
        return Result.success(loggedInUser);
    }
}