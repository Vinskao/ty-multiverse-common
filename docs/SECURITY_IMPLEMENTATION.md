# TY Multiverse Security 实施总结

## 概述

TY Multiverse 项目已成功实施统一的 Spring Security 配置框架，实现了 Gateway 和 Backend 的深度防御架构。

## 架构设计

### 分层安全模型

```
┌─────────────────────────────────────────────────────────┐
│                      Frontend                            │
│                   (Token Storage)                        │
└────────────────────┬────────────────────────────────────┘
                     │ JWT Token
                     ↓
┌─────────────────────────────────────────────────────────┐
│                  Gateway Security                        │
│              (粗粒度 - 路由级别)                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │ • Token 验证（Keycloak JWT）                      │  │
│  │ • 基础认证检查                                    │  │
│  │ • 路由级别权限控制                                │  │
│  │ • 公共路径放行                                    │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────────┘
                     │ Validated Token
                     ↓
┌─────────────────────────────────────────────────────────┐
│                 Backend Security                         │
│             (细粒度 - 方法级别)                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │ • Token 再验证（深度防御）                        │  │
│  │ • 方法级别权限控制 (@PreAuthorize)                │  │
│  │ • 数据级别权限控制                                │  │
│  │ • 业务逻辑安全验证                                │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
              Business Logic & Database
```

## 组件说明

### 1. Common 模块 (v1.7)

**位置**: `ty-multiverse-common/src/main/java/tw/com/ty/common/security/`

**提供的组件**:

#### BaseSecurityConfig
```java
@Configuration
public class BaseSecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    public static class SecurityConstants {
        // JWT 相关常量
        // 角色常量
        // 路径常量
    }
}
```

#### JwtTokenProvider
```java
@Component
public class JwtTokenProvider {
    // Token 生成
    public String generateToken(String username, Map<String, Object> claims);
    
    // Token 解析
    public String extractUsername(String token);
    public List<String> extractRoles(String token);
    
    // Token 验证
    public boolean validateToken(String token, String username);
}
```

#### SecurityExceptionHandler
```java
@RestControllerAdvice
public class SecurityExceptionHandler {
    // 处理认证异常
    @ExceptionHandler(AuthenticationException.class)
    
    // 处理授权异常
    @ExceptionHandler(AccessDeniedException.class)
    
    // 处理凭证异常
    @ExceptionHandler(BadCredentialsException.class)
}
```

### 2. Gateway Security

**位置**: `ty-multiverse-gateway/src/main/java/tw/com/tymgateway/config/SecurityConfig.java`

**职责** (粗粒度):
- ✅ Token 验证（所有 `/tymg/**` 路径）
- ✅ 基础认证检查
- ✅ 公共路径放行（健康检查、Swagger）
- ❌ **不做方法级别权限判断**

**配置示例**:
```java
@Configuration
@EnableWebFluxSecurity
@Import(BaseSecurityConfig.class)
public class SecurityConfig {
    
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange(exchanges -> exchanges
                // 公共路径
                .pathMatchers("/tymg/health/**").permitAll()
                .pathMatchers("/tymg/swagger-ui/**").permitAll()
                
                // 业务路径：需要 Token
                .pathMatchers("/tymg/people/**").authenticated()
                .pathMatchers("/tymg/weapons/**").authenticated()
                .pathMatchers("/tymg/gallery/**").authenticated()
                
                // 默认：需要认证
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(reactiveJwtDecoder()))
            )
            .build();
    }
}
```

### 3. Backend Security

**位置**: `ty-multiverse-backend/src/main/java/tw/com/tymbackend/core/config/security/BackendSecurityConfig.java`

**职责** (细粒度):
- ✅ Token 再验证（深度防御）
- ✅ 方法级别权限控制
- ✅ 数据级别权限控制
- ✅ 业务逻辑安全验证

**配置示例**:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Import(BaseSecurityConfig.class)
public class BackendSecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                // 公共路径
                .requestMatchers("/tymb/actuator/**").permitAll()
                .requestMatchers("/tymb/health/**").permitAll()
                
                // 认证路径
                .requestMatchers("/tymb/auth/visitor").permitAll()
                .requestMatchers("/tymb/keycloak/introspect").permitAll()
                
                // 业务路径：需要 Token（具体权限由 @PreAuthorize 控制）
                .requestMatchers("/tymb/people/**").authenticated()
                .requestMatchers("/tymb/weapons/**").authenticated()
                .requestMatchers("/tymb/gallery/**").authenticated()
                
                // 默认：需要认证
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder()))
            )
            .build();
    }
}
```

## 权限策略

### 基于 AGENTS.md 的端点定义

#### SELECT 系列：已认证即可访问

```java
@GetMapping("/get-all")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<BackendApiResponse<List<People>>> getAllPeople() {
    // 任何已认证用户都可以查询
}
```

#### INSERT/UPDATE/DELETE 系列：需要认证

```java
@PostMapping("/insert")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<BackendApiResponse<People>> insertPeople(@RequestBody People people) {
    // 需要认证才能插入
}

@PostMapping("/update")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<BackendApiResponse<People>> updatePeople(@RequestBody People people) {
    // 需要认证才能更新
}

@DeleteMapping("/delete/{id}")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<BackendApiResponse<Void>> deletePeople(@PathVariable Long id) {
    // 需要认证才能删除
}
```

#### 批量删除：仅管理员

```java
@DeleteMapping("/delete-all")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<BackendApiResponse<Void>> deleteAllPeople() {
    // 只有管理员可以批量删除
}
```

## 职责分工

### Gateway 的职责

| 职责 | 说明 | 实现方式 |
|------|------|---------|
| **Token 验证** | 验证 JWT Token 有效性 | OAuth2 Resource Server + Keycloak |
| **基础认证** | 确保请求携带有效 Token | `.authenticated()` |
| **路由权限** | 粗粒度的路由级别控制 | `.pathMatchers(...).authenticated()` |
| **公共路径** | 放行健康检查、Swagger | `.pathMatchers(...).permitAll()` |

### Backend 的职责

| 职责 | 说明 | 实现方式 |
|------|------|---------|
| **Token 再验证** | 深度防御，防止 Gateway 被绕过 | OAuth2 Resource Server + Keycloak |
| **方法权限** | 细粒度的方法级别控制 | `@PreAuthorize("...")` |
| **数据权限** | 资源所有者验证 | `@PreAuthorize("@service.isOwner(...)")` |
| **业务安全** | 业务逻辑相关的安全验证 | Service 层逻辑 |

### 为什么不重复？

虽然 Gateway 和 Backend 都验证 Token，但职责不同：

1. **Gateway**: 
   - **目的**: 统一入口，拦截无效请求
   - **粒度**: 粗粒度（路由级别）
   - **性能**: 在入口就拦截，减少 Backend 负载

2. **Backend**:
   - **目的**: 深度防御，防止 Gateway 被绕过
   - **粒度**: 细粒度（方法级别、数据级别）
   - **安全**: 即使 Gateway 被绕过，Backend 仍有保护

## Keycloak 集成

### Gateway 配置

```yaml
# application.yml
keycloak:
  auth-server-url: ${KEYCLOAK_URL}
  realm: ${KEYCLOAK_REALM}
  resource: ${KEYCLOAK_CLIENT_ID}

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}
          jwk-set-uri: ${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/certs
```

### Backend 配置

```yaml
# application.yml
keycloak:
  auth-server-url: ${KEYCLOAK_URL}
  realm: ${KEYCLOAK_REALM}
  resource: ${KEYCLOAK_CLIENT_ID}

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}
          jwk-set-uri: ${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/certs
```

## 测试验证

### 1. Gateway Token 验证

```bash
# 无 Token - 应该返回 401
curl http://localhost:8082/tymg/people/get-all

# 有效 Token - 应该转发到 Backend
curl -H "Authorization: Bearer <valid-token>" \
     http://localhost:8082/tymg/people/get-all
```

### 2. Backend 方法权限

```bash
# 普通用户查询 - 应该成功
curl -H "Authorization: Bearer <user-token>" \
     http://localhost:8080/tymb/people/get-all

# 普通用户批量删除 - 应该返回 403
curl -X DELETE \
     -H "Authorization: Bearer <user-token>" \
     http://localhost:8080/tymb/people/delete-all

# 管理员批量删除 - 应该成功
curl -X DELETE \
     -H "Authorization: Bearer <admin-token>" \
     http://localhost:8080/tymb/people/delete-all
```

## 部署清单

### ✅ 已完成

- [x] Common 模块 v1.7 发布
- [x] BaseSecurityConfig 创建
- [x] JwtTokenProvider 创建
- [x] SecurityExceptionHandler 创建
- [x] Gateway SecurityConfig 创建
- [x] Backend SecurityConfig 创建
- [x] SECURITY_ANNOTATIONS.md 文档
- [x] SECURITY_GUIDE.md 文档
- [x] SECURITY_IMPLEMENTATION.md 文档

### ⚠️ 待完成

- [ ] 更新所有 Backend Controller 添加 `@PreAuthorize` 注解
- [ ] 配置 Keycloak 环境变量
- [ ] 测试 Gateway Token 验证
- [ ] 测试 Backend 方法权限
- [ ] 测试深度防御（绕过 Gateway）
- [ ] 性能测试
- [ ] 安全审计

## 相关文档

- `ty-multiverse-common/SECURITY_GUIDE.md` - 安全配置指南
- `ty-multiverse-backend/SECURITY_ANNOTATIONS.md` - 注解使用指南
- `ty-multiverse-backend/AGENTS.md` - Backend 端点定义
- `ty-multiverse-gateway/AGENTS.md` - Gateway 架构说明
- `ty-multiverse-frontend/AGENTS.md` - API 架构说明

## 版本历史

- **v1.7 (2025-11-10)**: 初始版本，统一 Spring Security 配置框架
  - 创建 Common 安全组件
  - 实施 Gateway 和 Backend 安全配置
  - 基于 AGENTS.md 的权限策略
  - 深度防御架构

