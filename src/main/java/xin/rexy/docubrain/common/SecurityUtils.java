package xin.rexy.docubrain.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xin.rexy.docubrain.entity.User;


public class SecurityUtils {

    /**
     * 安全地获取当前用户ID
     * 优先从Spring Security上下文获取，如果没有则从Session获取
     */
    public static Long getUserId() {
        System.out.println("\n=== SecurityUtils.getUserId() 调试信息 ===");
        
        try {
            // 1. 尝试从 Spring Security 上下文获取
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Spring Security Authentication: " + authentication);
            
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                System.out.println("Authentication Principal: " + authentication.getPrincipal());
                System.out.println("Authentication Name: " + authentication.getName());
                // 这里可以根据实际的用户对象类型进行转换
                // 暂时返回默认值，后续可以扩展
            }
            
            // 2. 尝试从 Session 获取
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            System.out.println("ServletRequestAttributes: " + attr);
            
            if (attr != null) {
                HttpSession session = attr.getRequest().getSession(false);
                System.out.println("HttpSession: " + session);
                System.out.println("Session ID: " + (session != null ? session.getId() : "null"));
                
                if (session != null) {
                    Object userId = session.getAttribute("userId");
                    System.out.println("Session中的userId: " + userId);
                    System.out.println("Session中的所有属性:");
                    
                    // 打印所有Session属性
                    java.util.Enumeration<String> attributeNames = session.getAttributeNames();
                    while (attributeNames.hasMoreElements()) {
                        String name = attributeNames.nextElement();
                        Object value = session.getAttribute(name);
                        System.out.println("  " + name + " = " + value);
                    }
                    
                    if (userId != null) {
                        Long userIdLong = Long.valueOf(userId.toString());
                        System.out.println("从Session获取到用户ID: " + userIdLong);
                        return userIdLong;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("获取Session时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 3. 默认返回用户ID 1（开发环境）
        System.out.println("未找到用户ID，返回默认值: 1");
        System.out.println("=== SecurityUtils.getUserId() 调试信息结束 ===\n");
        return 1L;
    }
    
    /**
     * 设置用户ID到Session中
     */
    public static void setUserId(Long userId) {
        System.out.println("=== SecurityUtils.setUserId() 调试信息 ===");
        System.out.println("要设置的用户ID: " + userId);
        
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            System.out.println("ServletRequestAttributes: " + attributes);
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                HttpSession session = request.getSession(true);
                System.out.println("HttpSession: " + session);
                session.setAttribute("userId", userId);
                System.out.println("成功设置Session中的userId: " + userId);
                
                // 验证设置是否成功
                Object verifyUserId = session.getAttribute("userId");
                System.out.println("验证Session中的userId: " + verifyUserId);
            } else {
                System.out.println("ServletRequestAttributes为null，无法设置Session");
            }
        } catch (Exception e) {
            System.out.println("Session设置异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 清除用户Session
     */
    public static void clearUserSession() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.removeAttribute("userId");
                }
            }
        } catch (Exception e) {
            // 忽略Session清除异常
        }
    }
}
