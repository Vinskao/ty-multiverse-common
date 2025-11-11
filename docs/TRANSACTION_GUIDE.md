# TY Multiverse 事务管理指南

## 概述

TY Multiverse 项目统一了事务管理策略，提供了一套标准的事务注解和配置，确保跨项目的行为一致性。

## 核心组件

### 1. TyTransactional 注解

统一的事务注解，提供了多种预定义配置：

#### 基本使用

```java
import tw.com.ty.common.transaction.annotation.TyTransactional;

@Service
public class UserService {

    // 读操作 - 只读事务
    @TyTransactional.READ_ONLY
    public User getUser(String id) {
        return userRepository.findById(id);
    }

    // 写操作 - 标准事务（自动回滚 RuntimeException）
    @TyTransactional.REQUIRED
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // 业务操作 - 包含业务异常处理
    @TyTransactional.BUSINESS
    public User updateUser(User user) {
        // 业务逻辑
        if (businessRuleViolation) {
            throw new BusinessException("业务规则违反");
        }
        return userRepository.save(user);
    }

    // 批处理操作 - 较长超时时间
    @TyTransactional.BATCH
    public void batchUpdateUsers(List<User> users) {
        // 批量处理逻辑
        users.forEach(user -> userRepository.save(user));
    }

    // 新建事务 - 与当前事务完全隔离
    @TyTransactional.REQUIRES_NEW
    public void independentOperation() {
        // 这个操作会在新事务中执行
    }
}
```

#### 自定义配置

```java
@TyTransactional(
    propagation = TyTransactional.Propagation.REQUIRES_NEW,
    isolation = TyTransactional.Isolation.SERIALIZABLE,
    timeout = 60,
    rollbackFor = {CustomException.class},
    noRollbackFor = {ValidationException.class}
)
public void customTransaction() {
    // 自定义事务配置
}
```

### 2. TyTransactionConfig 配置类

全局事务配置，自动启用事务管理：

```java
@Configuration
@Import(TyTransactionConfig.class)
public class ApplicationConfig {
    // 配置会自动导入
}
```

## 事务策略

### Backend (数据层)

**位置**: 事务管理应在 Backend 的 Service 层

**理由**:
- Backend 直接操作数据库
- 事务边界应与业务逻辑保持一致
- Gateway 作为路由层，不应管理事务

**示例**:
```java
// Backend Service
@Service
@TyTransactional.READ_ONLY  // 类级别只读事务
public class PeopleService {

    @TyTransactional.REQUIRED  // 方法级别覆盖
    public People savePeople(People people) {
        return peopleRepository.save(people);
    }
}
```

### Gateway (路由层)

**位置**: Gateway 通常不需要事务管理

**理由**:
- Gateway 只做路由和过滤，不操作数据库
- 如果 Gateway 需要事务（极少见），应使用响应式事务管理

## 异常回滚规则

### 自动回滚 (rollbackFor)

- **RuntimeException**: 及其所有子类
  - `NullPointerException`
  - `IllegalArgumentException`
  - `IllegalStateException`
  - 数据库异常等

### 不自动回滚 (noRollbackFor)

- **检查型异常 (Checked Exceptions)**:
  - `IOException`
  - `SQLException`
  - 自定义检查型异常

- **业务异常 (根据配置)**:
  - `BusinessException` (在 BUSINESS 配置中会回滚)
  - `ValidationException` (通常不回滚)

### 配置建议

```java
// 推荐：明确的回滚配置
@TyTransactional.BUSINESS  // 业务异常会回滚

// 或者自定义
@TyTransactional(
    rollbackFor = {RuntimeException.class, BusinessException.class},
    noRollbackFor = {ValidationException.class}
)
```

## 性能优化

### 1. 读写分离

```java
@Repository
@TyTransactional.READ_ONLY  // 只读事务，优化查询性能
public interface UserRepository extends JpaRepository<User, Long> {
    // 查询方法自动使用只读事务
}
```

### 2. 批量操作

```java
@TyTransactional.BATCH  // 5分钟超时，适合批量操作
public void batchInsert(List<Entity> entities) {
    entities.forEach(entity -> repository.save(entity));
}
```

### 3. 事务传播

```java
@TyTransactional.REQUIRED
public void parentMethod() {
    // 调用子方法时会加入当前事务
    childMethod();
}

@TyTransactional.REQUIRES_NEW
public void childMethod() {
    // 总是开启新事务
}
```

## 监控和调试

### 启用事务日志

```yaml
logging:
  level:
    org.springframework.transaction: DEBUG
    org.springframework.orm.jpa: DEBUG
    tw.com.ty.common.transaction: DEBUG
```

### 常见问题排查

#### 1. 事务未提交

**问题**: 数据没有保存到数据库

**检查**:
- 方法是否标注了 `@Transactional`
- 是否抛出了未配置的异常导致回滚
- 事务管理器是否正确配置

#### 2. 事务超时

**问题**: 长时间运行的操作抛出超时异常

**检查**:
- 是否使用了 `@TyTransactional.BATCH` 配置更长的超时时间
- 数据库查询是否可以优化

#### 3. 死锁

**问题**: 并发操作导致死锁

**检查**:
- 事务隔离级别是否合适
- 数据库锁竞争是否合理

## 最佳实践

### 1. 事务边界

```java
// ✅ 推荐：事务边界与业务边界一致
@TyTransactional.REQUIRED
public void processOrder(Order order) {
    validateOrder(order);     // 验证
    updateInventory(order);   // 更新库存
    createInvoice(order);     // 生成发票
    sendNotification(order);  // 发送通知
}

// ❌ 避免：事务跨越多个业务操作
@TyTransactional.REQUIRED
public void complexOperation() {
    processOrder(order);
    processPayment(payment);
    sendEmail(email);  // 邮件发送失败不应回滚订单
}
```

### 2. 异常处理

```java
// ✅ 推荐：明确的异常处理
@TyTransactional.REQUIRED
public void updateUser(User user) {
    try {
        validateUser(user);
        userRepository.save(user);
    } catch (ValidationException e) {
        // 验证异常，不回滚
        log.warn("用户验证失败: {}", e.getMessage());
        throw e;
    } catch (RuntimeException e) {
        // 运行时异常，回滚
        log.error("更新用户失败", e);
        throw e;
    }
}
```

### 3. 测试事务

```java
@SpringBootTest
@Transactional  // 测试类级别事务
public class UserServiceTest {

    @Test
    @Rollback  // 测试后回滚
    public void testCreateUser() {
        // 测试逻辑
    }
}
```

## 版本历史

- **v1.0 (2025)**: 初始版本，统一事务管理策略
