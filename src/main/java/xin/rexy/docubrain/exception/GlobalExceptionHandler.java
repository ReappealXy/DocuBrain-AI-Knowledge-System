package xin.rexy.docubrain.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xin.rexy.docubrain.common.Result;

/**
 * 全局异常处理器
 *
 * @RestControllerAdvice 注解会自动捕获所有 @RestController 抛出的异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 专门用来处理业务逻辑异常 (IllegalArgumentException)
     * 比如：用户名已存在、密码错误等
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        // 当 Service 层抛出此异常时，会被这里捕获
        System.err.println("捕获到业务异常: " + e.getMessage());
        // 返回一个带有具体错误信息的 Result 对象
        return Result.error(400, e.getMessage());
    }

    /**
     * 兜底的异常处理器，用来处理所有其他未被捕获的异常
     * 比如：数据库连接失败、空指针等
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        // 在后台打印完整的错误堆栈，方便排查问题
        e.printStackTrace();
        // 向前端返回一个通用的、模糊的错误信息，避免泄露系统内部细节
        return Result.error(500, "服务器内部错误，请联系管理员");
    }
}