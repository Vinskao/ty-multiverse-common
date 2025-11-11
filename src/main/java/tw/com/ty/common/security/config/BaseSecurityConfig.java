package tw.com.ty.common.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 基礎安全配置
 *
 * <p>提供通用的 Spring Security 配置組件：</p>
 * <ul>
 *   <li>密碼編碼器</li>
 *   <li>安全工具類</li>
 *   <li>通用安全常量</li>
 * </ul>
 *
 * <p>子類可以繼承此配置並添加特定服務的安全規則</p>
 *
 * @author TY Backend Team
 * @version 1.0
 * @since 2025
 */
@Configuration
public class BaseSecurityConfig {

    /**
     * BCrypt 密碼編碼器
     * 用於密碼加密和驗證
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全配置常量
     */
    public static class SecurityConstants {
        // JWT 相關常量
        public static final String JWT_HEADER = "Authorization";
        public static final String JWT_PREFIX = "Bearer ";
        public static final String JWT_CLAIMS_ROLES = "roles";
        public static final String JWT_CLAIMS_USER_ID = "user_id";

        // 角色常量
        public static final String ROLE_ADMIN = "ROLE_ADMIN";
        public static final String ROLE_USER = "ROLE_USER";
        public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";

        // 路徑常量
        public static final String[] PUBLIC_PATHS = {
            "/actuator/**",
            "/health/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/webjars/**"
        };

        public static final String[] AUTH_PATHS = {
            "/auth/**",
            "/oauth2/**"
        };

        private SecurityConstants() {
            // 工具類不允許實例化
        }
    }
}
