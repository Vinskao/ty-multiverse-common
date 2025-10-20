package tw.com.ty.common.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * 統一請求響應日誌記錄 AOP
 *
 * 自動記錄所有 Controller 方法的請求和響應日誌
 * 支援 Spring WebMVC 和 WebFlux
 */
@Aspect
@Component
public class RequestResponseLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingAspect.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 攔截所有 RestController 方法
     */
    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();

        // 記錄請求開始
        logRequest(joinPoint, requestId);

        try {
            Object result = joinPoint.proceed();

            // 記錄成功響應
            logResponse(joinPoint, result, requestId, startTime, true);
            return result;

        } catch (Exception e) {
            // 記錄異常響應
            logResponse(joinPoint, e, requestId, startTime, false);
            throw e;
        }
    }

    private void logRequest(ProceedingJoinPoint joinPoint, String requestId) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                logger.info("🚀 [{}] {} {} - Started",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI()
                );

                // 記錄請求參數（排除敏感資訊）
                if (logger.isDebugEnabled()) {
                    String params = getRequestParameters(joinPoint);
                    logger.debug("📝 [{}] Request parameters: {}", requestId, params);
                }

                // 記錄請求頭
                if (logger.isDebugEnabled()) {
                    String headers = getRequestHeaders(request);
                    logger.debug("📋 [{}] Request headers: {}", requestId, headers);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to log request for {}: {}", joinPoint.getSignature().toShortString(), e.getMessage());
        }
    }

    private void logResponse(ProceedingJoinPoint joinPoint, Object result, String requestId, long startTime, boolean success) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            long duration = System.currentTimeMillis() - startTime;

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                if (success) {
                    logger.info("✅ [{}] {} {} - Completed in {}ms",
                        requestId,
                        request.getMethod(),
                        request.getRequestURI(),
                        duration
                    );
                } else {
                    logger.error("❌ [{}] {} {} - Failed in {}ms",
                        requestId,
                        request.getMethod(),
                        request.getRequestURI(),
                        duration
                    );
                }

                // 記錄響應內容（根據配置決定是否記錄）
                if (logger.isDebugEnabled() && result != null) {
                    String responseContent = truncateResponse(result);
                    logger.debug("📤 [{}] Response: {}", requestId, responseContent);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to log response for {}: {}", joinPoint.getSignature().toShortString(), e.getMessage());
        }
    }

    private String getRequestParameters(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 0) return "[]";

        // 過濾敏感參數
        return Arrays.toString(Arrays.stream(args)
            .map(arg -> {
                if (arg == null) return "null";
                String className = arg.getClass().getSimpleName();
                // 不記錄敏感類型，如 HttpServletRequest, HttpServletResponse 等
                if (className.contains("HttpServlet") || className.contains("Request") || className.contains("Response")) {
                    return "[" + className + "]";
                }
                return arg.toString();
            })
            .toArray());
    }

    private String getRequestHeaders(HttpServletRequest request) {
        // 只記錄重要的請求頭，避免記錄敏感資訊
        StringBuilder headers = new StringBuilder();
        headers.append("User-Agent: ").append(request.getHeader("User-Agent")).append(", ");
        headers.append("Content-Type: ").append(request.getHeader("Content-Type")).append(", ");
        headers.append("Content-Length: ").append(request.getContentLength());
        return headers.toString();
    }

    private String truncateResponse(Object result) {
        try {
            String json = objectMapper.writeValueAsString(result);
            // 限制響應內容長度，避免日誌過大
            if (json.length() > 1000) {
                return json.substring(0, 1000) + "... [truncated]";
            }
            return json;
        } catch (Exception e) {
            return result.getClass().getSimpleName() + " [cannot serialize]";
        }
    }
}
