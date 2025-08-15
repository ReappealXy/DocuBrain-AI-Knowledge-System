package xin.rexy.docubrain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加静态资源处理
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        
        // 处理favicon请求
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
        
        // 忽略开发工具相关的资源请求，避免404错误
        registry.addResourceHandler("/@vite/**")
                .addResourceLocations("classpath:/static/");
        
        // 忽略Chrome DevTools相关请求
        registry.addResourceHandler("/.well-known/**")
                .addResourceLocations("classpath:/static/");
        
        // 忽略client相关请求，避免404错误
        registry.addResourceHandler("/client/**")
                .addResourceLocations("classpath:/static/");
    }
}