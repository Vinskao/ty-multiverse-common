# TY Multiverse Spring Security 统一指南

## 概述

TY Multiverse 项目提供了统一的 Spring Security 配置框架，支持 Gateway 和 Backend 的安全需求。通过抽象公共组件，实现了安全配置的复用和一致性。

## 核心组件

### 1. BaseSecurityConfig - 基础安全配置

提供通用的安全组件：

```java
@Configuration
@Import(BaseSecurityConfig.class)  // 在应用配置中导入
public class AppConfig {
    // 配置会自动导入基础安全组件
}
```

#### 提供的组件

- **PasswordEncoder**: BCrypt 密码编码器
- **SecurityConstants**: 安全常量定义
  - JWT 相关常量
  - 角色常量
  - 路径常量

### 2. JwtTokenProvider - JWT Token 提供者

完整的 JWT 处理功能：

```java
@Component
public class JwtTokenProvider {
    // Token 生成、解析、验证
    // 用户信息提取
    // Token 过期检查
}
```

#### 主要功能

- **生成 Token**: `generateToken(username, claims)`
- **解析 Token**: `extractUsername(token)`, `extractRoles(token)`
- **验证 Token**: `validateToken(token, username)`
- **提取信息**: `extractUserId(token)`, `extractRoles(token)`

### 3. SecurityExceptionHandler - 安全异常处理器

统一的 Spring Security 异常处理：

```java
@RestControllerAdvice
public class SecurityExceptionHandler {
    // 处理认证异常
    // 处理授权异常
    // 处理凭证异常
}
```

## Backend 配置示例

### 完整的安全配置

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Import(BaseSecurityConfig.class)  // 导入通用配置
public class BackendSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // 公共路径
                .antMatchers(SecurityConstants.PUBLIC_PATHS).permitAll()
                // 认证路径
                .antMatchers(SecurityConstants.AUTH_PATHS).permitAll()
                // 其他路径需要认证
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler());
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
```

### JWT 认证过滤器

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = jwtTokenProvider.extractTokenFromHeader(
            request.getHeader(SecurityConstants.JWT_HEADER)
        );

        if (token != null && jwtTokenProvider.validateToken(token, getUsernameFromToken(token))) {
            UsernamePasswordAuthenticationToken authentication = getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        String username = jwtTokenProvider.extractUsername(token);
        List<String> roles = jwtTokenProvider.extractRoles(token);

        List<GrantedAuthority> authorities = roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
}
```

## Gateway 配置示例

### 简化的安全配置

```java
@Configuration
@Import(BaseSecurityConfig.class)  // 导入通用配置
public class GatewaySecurityConfig {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf().disable()
            .authorizeExchange()
                // 公共路径
                .pathMatchers(SecurityConstants.PUBLIC_PATHS).permitAll()
                // 健康检查路径
                .pathMatchers("/actuator/**").permitAll()
                // 其他路径需要认证
                .anyExchange().authenticated()
            .and()
            .addFilterAt(jwtReactiveAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .exceptionHandling()
                .authenticationEntryPoint(gatewayAuthenticationEntryPoint())
            .and()
            .build();
    }

    @Bean
    public JwtReactiveAuthenticationFilter jwtReactiveAuthenticationFilter() {
        return new JwtReactiveAuthenticationFilter(jwtTokenProvider);
    }
}
```

### 响应式 JWT 过滤器

```java
@Component
public class JwtReactiveAuthenticationFilter implements WebFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = extractTokenFromRequest(exchange.getRequest());

        if (token != null) {
            return validateAndSetAuthentication(token, exchange, chain);
        }

        return chain.filter(exchange);
    }

    private String extractTokenFromRequest(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(SecurityConstants.JWT_HEADER);
        return jwtTokenProvider.extractTokenFromHeader(authHeader);
    }

    private Mono<Void> validateAndSetAuthentication(String token, ServerWebExchange exchange, WebFilterChain chain) {
        try {
            if (jwtTokenProvider.validateToken(token, jwtTokenProvider.extractUsername(token))) {
                Authentication authentication = createAuthentication(token);
                return chain.filter(exchange)
                    .subscriberContext(ctx -> ctx.put(SecurityContext.class,
                        new SecurityContextImpl(authentication)));
            }
        } catch (Exception e) {
            // Token 无效，继续处理
        }

        return chain.filter(exchange);
    }

    private Authentication createAuthentication(String token) {
        String username = jwtTokenProvider.extractUsername(token);
        List<String> roles = jwtTokenProvider.extractRoles(token);

        List<GrantedAuthority> authorities = roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
}
```

## 配置文件

### Backend 配置 (application.yml)

```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 86400000  # 24小时

spring:
  security:
    user:
      name: ${ADMIN_USERNAME:admin}
      password: ${ADMIN_PASSWORD:password}
```

### Gateway 配置 (application.yml)

```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 86400000

logging:
  level:
    org.springframework.security: DEBUG
    tw.com.ty.common.security: DEBUG
```

## 使用示例

### 1. 控制器中的安全注解

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        // 只有 USER 或 ADMIN 角色可以访问
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        // 只有 ADMIN 角色可以访问
        return ResponseEntity.ok(userService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isOwner(#id, authentication.name)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // ADMIN 角色或资源所有者可以访问
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### 2. JWT Token 生成和使用

```java
@Service
public class AuthService {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String login(String username, String password) {
        // 验证用户名密码
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 生成 JWT Token
            Map<String, Object> claims = new HashMap<>();
            claims.put(SecurityConstants.JWT_CLAIMS_USER_ID, user.getId());
            claims.put(SecurityConstants.JWT_CLAIMS_ROLES, user.getRoles());

            return jwtTokenProvider.generateToken(username, claims);
        }
        throw new BadCredentialsException("Invalid username or password");
    }

    public boolean validateToken(String token) {
        try {
            return jwtTokenProvider.validateToken(token, jwtTokenProvider.extractUsername(token));
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 3. Gateway 路由级别的安全

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: secure-route
          uri: http://backend:8080
          predicates:
            - Path=/api/secure/**
          filters:
            - StripPrefix=1
          metadata:
            # 路由级别的权限配置
            requiredRoles: ["USER", "ADMIN"]

        - id: admin-route
          uri: http://backend:8080
          predicates:
            - Path=/api/admin/**
          filters:
            - StripPrefix=1
          metadata:
            requiredRoles: ["ADMIN"]
```

## 最佳实践

### 1. 密码安全

```java
@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(String username, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(username, encodedPassword);
        return userRepository.save(user);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
```

### 2. Token 刷新机制

```java
@Service
public class TokenService {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-expiration:604800000}") // 7天
    private long refreshTokenExpiration;

    public TokenPair generateTokens(String username, Map<String, Object> claims) {
        String accessToken = jwtTokenProvider.generateToken(username, claims);
        String refreshToken = generateRefreshToken(username);

        return new TokenPair(accessToken, refreshToken);
    }

    public String refreshAccessToken(String refreshToken) {
        // 验证 refresh token
        if (validateRefreshToken(refreshToken)) {
            String username = extractUsernameFromRefreshToken(refreshToken);
            return jwtTokenProvider.generateToken(username);
        }
        throw new InvalidTokenException("Invalid refresh token");
    }
}
```

### 3. 角色和权限管理

```java
@Service
public class PermissionService {

    public boolean hasPermission(String username, String permission) {
        User user = userRepository.findByUsername(username);
        return user.getRoles().stream()
            .anyMatch(role -> role.getPermissions().contains(permission));
    }

    public boolean hasAnyRole(String username, String... roles) {
        User user = userRepository.findByUsername(username);
        return user.getRoles().stream()
            .anyMatch(role -> Arrays.asList(roles).contains(role.getName()));
    }
}
```

## 安全配置检查清单

- ✅ **HTTPS 强制**: 生产环境必须使用 HTTPS
- ✅ **密码策略**: 强密码要求和定期更换
- ✅ **Token 过期**: 设置合理的 Token 过期时间
- ✅ **CSRF 保护**: 在需要的地方启用 CSRF 保护
- ✅ **CORS 策略**: 严格的跨域资源共享策略
- ✅ **安全头**: 启用安全响应头
- ✅ **审计日志**: 记录安全相关事件
- ✅ **异常处理**: 避免敏感信息泄露

## 故障排除

### 常见问题

#### 1. Token 验证失败

**问题**: JWT token 被拒绝

**检查**:
- Token 格式是否正确 (Bearer prefix)
- Token 是否过期
- Secret key 是否一致
- Token 是否被篡改

#### 2. 权限不足

**问题**: 用户无法访问受保护资源

**检查**:
- 用户角色是否正确
- 权限注解是否正确配置
- 角色映射是否正确

#### 3. CORS 问题

**问题**: 前端请求被浏览器阻止

**检查**:
- CORS 配置是否正确
- 允许的源、方法、头是否完整
- 预检请求是否正确处理

## 版本历史

- **v1.0 (2025)**: 初始版本，统一 Spring Security 配置框架
