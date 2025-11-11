# API Response 使用指南

## 📚 概述

TY Multiverse Common 提供了统一的 API 响应格式体系，包含三个核心类：

```
BaseApiResponse (抽象基类)
    ├── GatewayResponse (Gateway 专用)
    └── BackendApiResponse (Backend 专用)
```

## 🎯 设计理念

### 为什么需要两个不同的响应类？

| 特性 | GatewayResponse | BackendApiResponse |
|------|----------------|-------------------|
| **用途** | 网关层路由转发、熔断降级 | 业务数据处理、CRUD操作 |
| **特有字段** | `service`, `version`, `traceId`, `route` | `requestId`, `total`, `page`, `pageSize`, `error` |
| **使用场景** | 健康检查、路由信息、降级响应 | 数据查询、分页、异步操作 |
| **响应特点** | 轻量级，关注系统状态 | 丰富元数据，关注业务数据 |

---

## 🔧 Gateway 使用示例

### 1. 基础成功响应

```java
@GetMapping("/health")
public ResponseEntity<GatewayResponse<Map<String, String>>> health() {
    Map<String, String> healthData = Map.of(
        "status", "UP",
        "uptime", "24h"
    );
    return ResponseEntity.ok(GatewayResponse.success(healthData));
}
```

**响应示例：**
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "timestamp": "2025-11-10T15:30:00Z",
  "service": "TY Multiverse Gateway",
  "version": "1.0.0",
  "data": {
    "status": "UP",
    "uptime": "24h"
  }
}
```

### 2. 熔断降级响应

```java
@GetMapping("/fallback")
public Mono<ResponseEntity<GatewayResponse<Void>>> getFallback() {
    GatewayResponse<Void> response = GatewayResponse.serviceUnavailable(
        "后端服务暂时不可用，请稍后再试"
    );
    return Mono.just(ResponseEntity.status(503).body(response));
}
```

**响应示例：**
```json
{
  "success": false,
  "code": 503,
  "message": "后端服务暂时不可用，请稍后再试",
  "timestamp": "2025-11-10T15:30:00Z",
  "service": "TY Multiverse Gateway",
  "version": "1.0.0"
}
```

### 3. 带追踪ID的响应

```java
@GetMapping("/routes")
public ResponseEntity<GatewayResponse<Map<String, Object>>> getRoutes(
    @RequestHeader(value = "X-Trace-Id", required = false) String traceId
) {
    Map<String, Object> routes = getRouteInfo();
    
    GatewayResponse<Map<String, Object>> response = GatewayResponse
        .success("路由信息获取成功", routes)
        .withTraceId(traceId)
        .withRoute("/api-docs/routes");
    
    return ResponseEntity.ok(response);
}
```

**响应示例：**
```json
{
  "success": true,
  "code": 200,
  "message": "路由信息获取成功",
  "timestamp": "2025-11-10T15:30:00Z",
  "service": "TY Multiverse Gateway",
  "version": "1.0.0",
  "traceId": "abc-123-def",
  "route": "/api-docs/routes",
  "data": {
    "routes": [...]
  }
}
```

### 4. 网关超时响应

```java
@GetMapping("/timeout-test")
public ResponseEntity<GatewayResponse<Void>> timeoutTest() {
    return ResponseEntity.status(504)
        .body(GatewayResponse.gatewayTimeout("请求超时，请稍后重试"));
}
```

---

## 🔧 Backend 使用示例

### 1. 基础成功响应

```java
@GetMapping("/weapons")
public ResponseEntity<BackendApiResponse<List<WeaponResponseDTO>>> getAllWeapons() {
    List<WeaponResponseDTO> weapons = weaponService.findAll();
    return ResponseEntity.ok(BackendApiResponse.success(weapons));
}
```

**响应示例：**
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "timestamp": "2025-11-10T15:30:00Z",
  "data": [
    {
      "owner": "角色A",
      "weapon": "神剑",
      "baseDamage": 100
    }
  ]
}
```

### 2. 分页响应

```java
@GetMapping("/people")
public ResponseEntity<BackendApiResponse<List<PeopleResponseDTO>>> getPeople(
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "20") int pageSize
) {
    Page<People> peoplePage = peopleService.findAll(page, pageSize);
    List<PeopleResponseDTO> people = convertToDTO(peoplePage.getContent());
    
    return ResponseEntity.ok(
        BackendApiResponse.successWithPagination(
            people,
            peoplePage.getTotalElements(),
            page,
            pageSize
        )
    );
}
```

**响应示例：**
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "timestamp": "2025-11-10T15:30:00Z",
  "total": 100,
  "page": 1,
  "pageSize": 20,
  "data": [...]
}
```

### 3. 异步请求接受响应

```java
@PostMapping("/people/get-all")
public ResponseEntity<BackendApiResponse<Void>> getAllPeopleAsync() {
    String requestId = UUID.randomUUID().toString();
    asyncMessageService.sendAsyncRequest(requestId, "GET_ALL_PEOPLE");
    
    return ResponseEntity.accepted()
        .body(BackendApiResponse.accepted(
            requestId, 
            "请求已接受，正在处理中"
        ));
}
```

**响应示例：**
```json
{
  "success": true,
  "code": 202,
  "message": "请求已接受，正在处理中",
  "timestamp": "2025-11-10T15:30:00Z",
  "requestId": "req-12345-67890"
}
```

### 4. 创建成功响应

```java
@PostMapping("/weapons")
public ResponseEntity<BackendApiResponse<WeaponResponseDTO>> createWeapon(
    @RequestBody WeaponRequestDTO request
) {
    WeaponResponseDTO weapon = weaponService.create(request);
    return ResponseEntity.status(201)
        .body(BackendApiResponse.created(weapon));
}
```

**响应示例：**
```json
{
  "success": true,
  "code": 201,
  "message": "Created successfully",
  "timestamp": "2025-11-10T15:30:00Z",
  "data": {
    "owner": "新角色",
    "weapon": "新武器",
    "baseDamage": 50
  }
}
```

### 5. 参数验证失败响应

```java
@PostMapping("/people/insert")
public ResponseEntity<BackendApiResponse<Void>> insertPeople(
    @RequestBody PeopleRequestDTO request
) {
    if (request.getName() == null || request.getName().isEmpty()) {
        return ResponseEntity.badRequest()
            .body(BackendApiResponse.badRequest("人物名称不能为空"));
    }
    
    // ... 处理逻辑
}
```

**响应示例：**
```json
{
  "success": false,
  "code": 400,
  "message": "人物名称不能为空",
  "timestamp": "2025-11-10T15:30:00Z"
}
```

### 6. 资源未找到响应

```java
@GetMapping("/people/{name}")
public ResponseEntity<BackendApiResponse<PeopleResponseDTO>> getPersonByName(
    @PathVariable String name
) {
    Optional<People> person = peopleService.findByName(name);
    
    if (person.isEmpty()) {
        return ResponseEntity.status(404)
            .body(BackendApiResponse.notFound("未找到名为 '" + name + "' 的人物"));
    }
    
    return ResponseEntity.ok(
        BackendApiResponse.success(convertToDTO(person.get()))
    );
}
```

**响应示例：**
```json
{
  "success": false,
  "code": 404,
  "message": "未找到名为 '张三' 的人物",
  "timestamp": "2025-11-10T15:30:00Z"
}
```

### 7. 服务器错误响应（带详情）

```java
@PostMapping("/people/update")
public ResponseEntity<BackendApiResponse<Void>> updatePeople(
    @RequestBody PeopleRequestDTO request
) {
    try {
        peopleService.update(request);
        return ResponseEntity.ok(BackendApiResponse.success("更新成功"));
    } catch (Exception e) {
        log.error("更新人物失败", e);
        
        return ResponseEntity.status(500)
            .body(BackendApiResponse.internalError(
                "更新失败",
                e.getMessage()
            ));
    }
}
```

**响应示例：**
```json
{
  "success": false,
  "code": 500,
  "message": "更新失败",
  "timestamp": "2025-11-10T15:30:00Z",
  "error": "Database connection timeout"
}
```

### 8. Builder 模式链式调用

```java
@GetMapping("/people/search")
public ResponseEntity<BackendApiResponse<List<PeopleResponseDTO>>> searchPeople(
    @RequestParam String keyword,
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "20") int pageSize,
    @RequestHeader(value = "X-Request-Id", required = false) String requestId
) {
    SearchResult<People> result = peopleService.search(keyword, page, pageSize);
    
    BackendApiResponse<List<PeopleResponseDTO>> response = BackendApiResponse
        .success("搜索成功", convertToDTO(result.getData()))
        .withRequestId(requestId)
        .withPagination(result.getTotal(), page, pageSize);
    
    return ResponseEntity.ok(response);
}
```

**响应示例：**
```json
{
  "success": true,
  "code": 200,
  "message": "搜索成功",
  "timestamp": "2025-11-10T15:30:00Z",
  "requestId": "req-search-001",
  "total": 50,
  "page": 1,
  "pageSize": 20,
  "data": [...]
}
```

---

## 📋 完整的静态方法列表

### GatewayResponse

| 方法 | 返回码 | 说明 |
|------|--------|------|
| `success(T data)` | 200 | 成功响应 |
| `success(String message, T data)` | 200 | 成功响应（自定义消息） |
| `error(int code, String message)` | 自定义 | 错误响应 |
| `error(HttpStatus status, String message)` | 自定义 | 错误响应（使用HttpStatus） |
| `serviceUnavailable(String message)` | 503 | 服务不可用（熔断降级） |
| `gatewayTimeout(String message)` | 504 | 网关超时 |

### BackendApiResponse

| 方法 | 返回码 | 说明 |
|------|--------|------|
| `success(T data)` | 200 | 成功响应 |
| `success(String message, T data)` | 200 | 成功响应（自定义消息） |
| `success(String message)` | 200 | 成功响应（无数据） |
| `successWithPagination(...)` | 200 | 分页成功响应 |
| `accepted(String requestId, String message)` | 202 | 异步请求已接受 |
| `created(T data)` | 201 | 创建成功 |
| `error(int code, String message)` | 自定义 | 错误响应 |
| `error(HttpStatus status, String message)` | 自定义 | 错误响应（使用HttpStatus） |
| `error(HttpStatus status, String message, String error)` | 自定义 | 错误响应（带详情） |
| `badRequest(String message)` | 400 | 参数验证失败 |
| `unauthorized(String message)` | 401 | 未授权 |
| `forbidden(String message)` | 403 | 禁止访问 |
| `notFound(String message)` | 404 | 资源未找到 |
| `internalError(String message)` | 500 | 服务器内部错误 |
| `internalError(String message, String error)` | 500 | 服务器内部错误（带详情） |

---

## 🎨 最佳实践

### 1. 统一使用响应类

```java
// ✅ 推荐
return ResponseEntity.ok(BackendApiResponse.success(data));

// ❌ 避免
return ResponseEntity.ok(data);  // 缺少统一格式
```

### 2. 正确使用HTTP状态码

```java
// ✅ 推荐：状态码与响应体一致
return ResponseEntity.status(404)
    .body(BackendApiResponse.notFound("资源未找到"));

// ❌ 避免：状态码与响应体不一致
return ResponseEntity.ok()  // 200
    .body(BackendApiResponse.notFound("资源未找到"));  // code: 404
```

### 3. 异步操作使用 202 Accepted

```java
// ✅ 推荐
return ResponseEntity.accepted()
    .body(BackendApiResponse.accepted(requestId, "正在处理"));

// ❌ 避免
return ResponseEntity.ok()  // 不应该用 200
    .body(BackendApiResponse.accepted(requestId, "正在处理"));
```

### 4. 分页查询必须包含元数据

```java
// ✅ 推荐
return ResponseEntity.ok(
    BackendApiResponse.successWithPagination(data, total, page, pageSize)
);

// ❌ 避免
return ResponseEntity.ok(BackendApiResponse.success(data));  // 缺少分页信息
```

### 5. 错误响应应包含详情

```java
// ✅ 推荐
catch (ValidationException e) {
    return ResponseEntity.badRequest()
        .body(BackendApiResponse.badRequest(e.getMessage())
            .withError(e.getDetails()));
}

// ❌ 避免
catch (Exception e) {
    return ResponseEntity.badRequest()
        .body(BackendApiResponse.badRequest("错误"));  // 信息不足
}
```

---

## 🔄 迁移指南

### 从旧格式迁移到新格式

**旧代码：**
```java
@GetMapping("/weapons")
public ResponseEntity<List<WeaponResponseDTO>> getAllWeapons() {
    return ResponseEntity.ok(weaponService.findAll());
}
```

**新代码：**
```java
@GetMapping("/weapons")
public ResponseEntity<BackendApiResponse<List<WeaponResponseDTO>>> getAllWeapons() {
    List<WeaponResponseDTO> weapons = weaponService.findAll();
    return ResponseEntity.ok(BackendApiResponse.success(weapons));
}
```

**响应变化：**

旧格式：
```json
[
  {"owner": "角色A", "weapon": "神剑"}
]
```

新格式：
```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "timestamp": "2025-11-10T15:30:00Z",
  "data": [
    {"owner": "角色A", "weapon": "神剑"}
  ]
}
```

---

## 📝 总结

- **Gateway** 使用 `GatewayResponse` - 关注系统状态和路由信息
- **Backend** 使用 `BackendApiResponse` - 关注业务数据和元数据
- **统一格式** - 所有响应都包含 `success`, `code`, `message`, `timestamp`, `data`
- **类型安全** - 使用泛型 `<T>` 确保数据类型安全
- **Builder模式** - 支持链式调用，方便扩展
- **静态工厂** - 提供丰富的静态方法，简化使用

---

**版本：** 1.0.0  
**更新日期：** 2025-11-10  
**作者：** TY Backend Team

