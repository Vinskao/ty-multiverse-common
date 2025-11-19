# TY Multiverse Common - Agent Guide

## 📁 文档组织规定

**重要**：所有非 `AGENTS.md` 和 `README.md` 的 Markdown 文档都必须放在项目的 `/docs` 目录下。

- ✅ **允许在根目录**：`AGENTS.md`、`README.md`
- ✅ **必须放在 `/docs`**：所有其他 `.md` 文件（如 `QUICK_START.md`、`SECURITY_GUIDE.md` 等）
- 📂 **文档目录结构**：`/docs/` 目录下可以创建子目录来组织相关文档

## Project Overview

TY Multiverse Common is a shared library module that provides common utilities, exception handling, logging aspects, and other reusable components for the TY Multiverse ecosystem. This module is designed to be shared across multiple services in the TY Multiverse system.

### Architecture
- **Framework**: Java 21 with Maven
- **Purpose**: Shared utilities and common functionality
- **Distribution**: Published to GitHub Packages
- **Consumers**: Backend, Gateway, Consumer services

### Key Components
- **Exception Handling**: Unified exception management for REST and gRPC
- **AOP Logging**: Automatic request/response logging
- **Error Codes**: Standardized error code management
- **DTOs**: Common data transfer objects
- **Utilities**: Helper classes and utilities

## Build and Test Commands

### Prerequisites
```bash
# Ensure Java 21 is installed
java -version

# Verify Maven installation
./mvnw --version
```

### Build Commands
```bash
# Clean and compile
./mvnw clean compile

# Run tests
./mvnw test

# Package JAR
./mvnw package

# Install to local repository
./mvnw install

# Clean everything
./mvnw clean
```

### Publishing to GitHub Packages
```bash
# Set GitHub token as environment variable
export GITHUB_TOKEN=your_github_token_here

# Deploy to GitHub Packages
./mvnw deploy

# Alternative: Maven automatic publishing (recommended)
# Configure GITHUB_TOKEN in CI/CD pipeline
```

### Development Workflow
```bash
# Make changes in common module
# Build and install to local repository
./mvnw clean install

# Other projects automatically use updated version
cd ../ty-multiverse-backend
./mvnw compile  # Uses new common version
```

## Code Style Guidelines

### Java Code Style
- **Language Level**: Java 21
- **Formatting**: Standard Java conventions with 4-space indentation
- **Line Length**: Max 100 characters for better readability
- **Naming Conventions**:
  - Classes: PascalCase
  - Methods/Variables: camelCase
  - Constants: SCREAMING_SNAKE_CASE

### Package Structure
```
src/main/java/tw/com/ty/common/
├── exception/      # Exception handling framework
│   ├── advice/     # AOP exception advice
│   ├── handler/    # Exception handlers
│   └── impl/       # Implementation classes
├── logging/       # AOP logging aspects
├── dto/          # Data transfer objects
└── utils/        # Utility classes
```

### Code Quality Standards
```java
// ✅ Good: Clean, readable code
@Slf4j
@Service
@RequiredArgsConstructor
public class ExceptionHandler {

    private final ErrorCodeMapper errorCodeMapper;

    public ErrorResponse handleException(Exception ex) {
        log.error("Handling exception: {}", ex.getMessage(), ex);
        return ErrorResponse.builder()
                .errorCode(errorCodeMapper.map(ex))
                .message("An error occurred")
                .timestamp(Instant.now())
                .build();
    }
}

// ❌ Avoid: Poor formatting and structure
@Service public class ExceptionHandler{public ErrorResponse handleException(Exception ex){...}}
```

## Testing Instructions

### Unit Tests
- **Focus**: Test individual utility methods and components
- **Framework**: JUnit 5 with Mockito
- **Coverage**: Aim for > 90% coverage for utility classes
- **Mocking**: Use Mockito for external dependencies

### Integration Tests
- **Scope**: Test AOP aspects and exception handling flows
- **Setup**: Use Spring Boot test context
- **Verification**: Ensure aspects are properly applied

### Test Examples
```java
@Test
void testExceptionMapping() {
    // Given
    RuntimeException ex = new RuntimeException("Test error");

    // When
    ErrorResponse response = exceptionHandler.handleException(ex);

    // Then
    assertEquals("INTERNAL_SERVER_ERROR", response.getErrorCode());
    assertNotNull(response.getTimestamp());
}
```

## Security Considerations

### Code Security
- **Input Validation**: Validate all inputs in utility methods
- **Exception Safety**: Avoid exposing internal system details in error messages
- **Resource Management**: Ensure proper resource cleanup

### Dependency Security
- **Vulnerability Scanning**: Regular dependency checks
- **Version Management**: Keep dependencies updated
- **Security Headers**: Ensure secure defaults

## Additional Instructions

### Commit Message Guidelines
```bash
# Format: <type>(<scope>): <description>

feat(exception): add new exception type for validation errors
fix(logging): resolve AOP aspect ordering issue
docs(readme): update documentation for error handling
test(utils): add unit tests for string utilities
refactor(dto): improve error response structure
```

### Pull Request Process
1. **Small Changes**: Keep PRs focused on single features/fixes
2. **Testing**: All tests must pass
3. **Documentation**: Update relevant documentation
4. **Review**: Require at least one approval

### Version Management
```bash
# Update version in pom.xml
<version>1.2.0</version>

# Commit version bump
git add pom.xml
git commit -m "chore: bump version to 1.2.0"

# Tag release
git tag v1.2.0
git push origin v1.2.0
```

### Dependency Updates
```bash
# Check for outdated dependencies
./mvnw versions:display-dependency-updates

# Update specific dependency
./mvnw versions:use-latest-versions -Dincludes=groupId:artifactId

# Commit dependency updates
git add pom.xml
git commit -m "chore(deps): update dependencies"
```

### Publishing Workflow
1. **Test Thoroughly**: Run full test suite before publishing
2. **Update Version**: Bump version appropriately (patch/minor/major)
3. **Deploy**: Publish to GitHub Packages
4. **Tag Release**: Create git tag for version
5. **Notify Consumers**: Inform other teams of updates

### Troubleshooting
- **Build Issues**: Clear local repository if dependency issues occur
- **Test Failures**: Check for breaking changes in dependencies
- **Publishing Issues**: Verify GITHUB_TOKEN and permissions
- **IDE Issues**: Refresh Maven projects if IntelliJ/Eclipse shows errors

### Environment Setup
```bash
# Required for development
export JAVA_HOME=/path/to/java21
export MAVEN_HOME=/path/to/maven
export GITHUB_TOKEN=your_token_here

# Optional for IDE support
export MAVEN_OPTS="-Xmx2g -XX:+UseG1GC"
```
