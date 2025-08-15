package xin.rexy.docubrain.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 处理Chrome DevTools相关请求的控制器
 * 避免开发环境中的404错误
 */
@RestController
public class DevToolsController {
    
    @GetMapping({
        "appspecific/com.chrome.devtools.json",
        "/appspecific/com.chrome.devtools.json"
    })
    public ResponseEntity<String> handleChromeDevTools() {
        // 返回空JSON对象，避免404错误
        return ResponseEntity.ok("{}");
    }
}