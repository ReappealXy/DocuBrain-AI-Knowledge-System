package xin.rexy.docubrain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests ->
                // --- 关键修改点 ---
                // .requestMatchers("/**") 匹配所有URL路径
                // .permitAll() 允许所有请求，无论是否登录
                authorizeRequests.requestMatchers("/**").permitAll()
            )
            // 禁用 CSRF 保护，这对于 API 测试是必需的
            .csrf(csrf -> csrf.disable()); 
            
        return http.build();
    }
}