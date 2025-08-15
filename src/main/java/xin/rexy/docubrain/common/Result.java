package xin.rexy.docubrain.common;

//import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
//@Schema(description = "全局统一返回结果")
public class Result<T> {

    //@Schema(description = "状态码", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Integer code; // 200 表示成功，其他表示失败

    //@Schema(description = "返回信息", requiredMode = Schema.RequiredMode.REQUIRED, example = "操作成功")
    private String message;

    //@Schema(description = "返回数据")
    private T data; // 使用泛型，可以返回任意类型的数据

    // 私有化构造函数，强制使用静态方法创建实例
    private Result() {}

    // ------------------- 静态工厂方法 -------------------

    /**
     * 成功，并返回数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功，不返回数据
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败，返回自定义错误码和信息
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败，返回默认的 400 错误码和自定义信息
     */
    public static <T> Result<T> error(String message) {
        return error(400, message);
    }
}