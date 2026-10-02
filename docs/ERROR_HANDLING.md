# Shared error handling (since 2.3.0)

One error-handling core, two thin web adapters. Any Spring Boot app that has `ty-multiverse-common` on the
classpath gets the right one automatically (`CommonExceptionAutoConfiguration`).

```
exception ──► ExceptionTranslator ──► Translation(HttpStatus, ErrorResponse)
                 │  chain of ApiExceptionHandler (first match wins)
                 │  Business → Security → Validation → SpringWeb → DataIntegrity
                 │  → OptimisticLock → Jdk → Resilience → Default
                 ▼
   ServletExceptionAdvice   (Spring MVC)     body: BackendApiResponse  (success=false, code, message, error)
   ReactiveExceptionAdvice  (Spring WebFlux) body: ErrorResponse       (code, message, detail, path, timestamp)
```

| Exception | Status |
|---|---|
| `BusinessException` | `ErrorCode.getHttpStatus()` |
| Spring Security `AccessDeniedException` / `AuthenticationException` | 403 / 401 |
| `MethodArgumentNotValidException`, unreadable JSON, missing parameter | 400 |
| Anything implementing Spring's `ErrorResponse` (405, 415, `ResponseStatusException`, ...) | its own status |
| `DataIntegrityViolationException`, optimistic-lock failures | 409 |
| `IllegalArgumentException` / `UnsupportedOperationException` / `SecurityException` | 400 / 400 / 403 |
| message mentions rate limit / timeout / circuit breaker | 429 / 504 / 429 |
| everything else | 500 |

## Using it
- Throw `BusinessException(ErrorCode.X, detail)` from services/controllers; do not hand-build error
  `ResponseEntity`s in try/catch blocks.
- Do not declare your own `@ExceptionHandler(Exception.class)` advice: two catch-alls make the winner undefined.
  Controller-local `@ExceptionHandler`s for specific types still take precedence.
- Disable with `ty.common.exception.enabled=false`; override by defining your own `ExceptionTranslator`
  or advice bean. Add a handler by building `new ExceptionTranslator(List.of(..., new MyHandler(), ...))`.
- Handlers for optional libraries (security, ORM) match by class name, so the library still loads in apps
  that do not have them (e.g. the WebFlux consumer has no Spring Security).

## Why not one advice class?
The servlet adapter needs `HttpServletRequest` and the reactive one `ServerHttpRequest`; they cannot be the
same class. `spring-webmvc` is `optional` in this library so a WebFlux app does not get it transitively.

## Deprecated (not registered any more)
`exception.advice.GlobalExceptionHandler` (mapped everything except `BusinessException` to 500),
`security.handler.SecurityExceptionHandler`, `exception.handler.ExceptionHandlerFactory`.

## Sensitive-data masking (since 2.3.0)

`SensitiveDataMasker.mask(text)` hides: `user:password@` in connection strings (jdbc, postgres, redis, amqp, ...),
`Bearer`/`Basic` credentials, bare JWTs, GitHub tokens, values of credential-like keys
(`password`, `secret`, `*token`, `api-key`, `authorization`, `credentials`, `private-key`; works for `k=v`, `k: v`
and JSON), e-mails (`a***@example.com`) and Taiwan national IDs (`A********9`). It is idempotent.

Where it is applied:

| Layer | What happens |
|---|---|
| Error responses (`ExceptionTranslator`) | **5xx**: generic message + `errorId` only (set `ty.common.exception.expose-server-error-detail=true` locally to also see the masked message). **4xx**: masked message. |
| Error logs (`ExceptionTranslator`) | Same `errorId` as the response; message and every cause message masked via `MaskedException`, stack frames kept. |
| `RequestResponseLoggingAspect` | Arguments logged as `name=value` (so `refreshToken=***` works for opaque tokens), response bodies and `error` fields masked. |
| Every log line (opt-in, recommended) | `logback-masking.xml` replaces `%m`/`%msg` and the Spring Boot stack-trace word with masking converters. |

Enable the last layer in an application with `src/main/resources/logback-spring.xml`:

```xml
<configuration>
    <include resource="tw/com/ty/common/logging/logback-masking.xml"/>
    <include resource="org/springframework/boot/logging/logback/base.xml"/>
</configuration>
```

Check that the build actually packages it (`unzip -l app.jar | grep logback-spring.xml`): a `<resources>` whitelist in the
pom can silently drop `.xml` files.

Masking is pattern based: it cannot recognise an opaque secret that has no key and no known format. Do not put
secrets in exception messages or log them on purpose; masking is the safety net, not the policy.
