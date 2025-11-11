# 🚀 API Response 快速开始

## 📦 安装

Common 模块已经作为依赖被 Gateway 和 Backend 引用，无需额外配置。

## 🎯 5分钟快速上手

### Gateway 示例

#### 1. 健康检查端点

```java
import tw.com.ty.common.response.GatewayResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    
    @GetMapping("/health")
    public ResponseEntity<GatewayResponse<Map<String, String>>> health() {
        Map<String, String> data = Map.of(
            "status", "UP",
            "service", "TY Multiverse Gateway"
        );
        
        return ResponseEntity.ok(GatewayResponse.success(data));
    }
}
```

#### 2. 熔断降级

```java
import tw.com.ty.common.response.GatewayResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    @GetMapping
    public Mono<ResponseEntity<GatewayResponse<Void>>> getFallback() {
        return Mono.just(
            ResponseEntity.status(503)
                .body(GatewayResponse.serviceUnavailable("后端服务暂时不可用"))
        );
    }
}
```

---

### Backend 示例

#### 1. 简单查询

```java
import tw.com.ty.common.response.BackendApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weapons")
public class WeaponController {
    
    @Autowired
    private WeaponService weaponService;
    
    @GetMapping
    public ResponseEntity<BackendApiResponse<List<WeaponResponseDTO>>> getAllWeapons() {
        List<WeaponResponseDTO> weapons = weaponService.findAll();
        return ResponseEntity.ok(BackendApiResponse.success(weapons));
    }
}
```

#### 2. 分页查询

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

#### 3. 异步操作

```java
@PostMapping("/people/get-all")
public ResponseEntity<BackendApiResponse<Void>> getAllPeopleAsync() {
    String requestId = UUID.randomUUID().toString();
    asyncMessageService.sendAsyncRequest(requestId, "GET_ALL_PEOPLE");
    
    return ResponseEntity.accepted()
        .body(BackendApiResponse.accepted(requestId, "请求已接受，正在处理中"));
}
```

#### 4. 错误处理

```java
@PostMapping("/people/insert")
public ResponseEntity<BackendApiResponse<Void>> insertPeople(
    @RequestBody PeopleRequestDTO request
) {
    // 参数验证
    if (request.getName() == null || request.getName().isEmpty()) {
        return ResponseEntity.badRequest()
            .body(BackendApiResponse.badRequest("人物名称不能为空"));
    }
    
    try {
        peopleService.insert(request);
        return ResponseEntity.ok(BackendApiResponse.success("插入成功"));
    } catch (DuplicateException e) {
        return ResponseEntity.badRequest()
            .body(BackendApiResponse.badRequest("人物已存在"));
    } catch (Exception e) {
        log.error("插入失败", e);
        return ResponseEntity.status(500)
            .body(BackendApiResponse.internalError("插入失败", e.getMessage()));
    }
}
```

---

## 📊 响应格式对比

### Gateway 响应

```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "timestamp": "2025-11-10T15:30:00Z",
  "service": "TY Multiverse Gateway",
  "version": "1.0.0",
  "traceId": "abc-123",
  "data": {...}
}
```

### Backend 响应

```json
{
  "success": true,
  "code": 200,
  "message": "Success",
  "timestamp": "2025-11-10T15:30:00Z",
  "requestId": "req-456",
  "total": 100,
  "page": 1,
  "pageSize": 20,
  "data": [...]
}
```

---

## 🔑 关键要点

1. **Gateway 用 `GatewayResponse`** - 网关特定功能
2. **Backend 用 `BackendApiResponse`** - 业务数据处理
3. **统一格式** - 所有响应都包含 `success`, `code`, `message`, `timestamp`
4. **类型安全** - 使用泛型 `<T>` 确保类型安全
5. **静态方法** - 使用静态工厂方法快速创建响应

---

## 📚 更多信息

查看 [RESPONSE_USAGE.md](./RESPONSE_USAGE.md) 获取完整的使用指南和示例。

---

**版本：** 1.0.0  
**作者：** TY Backend Team

