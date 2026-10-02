package tw.com.ty.common.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tw.com.ty.common.security.mask.SensitiveDataMasker;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
            logger.warn("Failed to log request for {}: {}", joinPoint.getSignature().toShortString(),
                SensitiveDataMasker.mask(e.getMessage()));
        }
    }

    private void logResponse(ProceedingJoinPoint joinPoint, Object result, String requestId, long startTime, boolean success) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            long duration = System.currentTimeMillis() - startTime;

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                HttpServletResponse response = attributes.getResponse();
                int statusCode = (response != null) ? response.getStatus() : (success ? 200 : 500);

                if (success) {
                    logger.info("✅ [{}] {} {} - Completed in {}ms (Status: {})",
                        requestId,
                        request.getMethod(),
                        request.getRequestURI(),
                        duration,
                        statusCode
                    );
                } else {
                    logger.error("❌ [{}] {} {} - Failed in {}ms (Status: {})",
                        requestId,
                        request.getMethod(),
                        request.getRequestURI(),
                        duration,
                        statusCode
                    );
                }

                // 記錄響應內容（根據配置決定是否記錄）
                if (logger.isDebugEnabled() && result != null) {
                    String responseContent = truncateResponse(result, statusCode);

                    // 根據狀態碼決定日誌級別
                    if (statusCode >= 200 && statusCode < 300) {
                        // 2xx 成功響應：使用 debug 級別
                        logger.debug("📤 [{}] Response: {}", requestId, responseContent);
                    } else {
                        // 3xx, 4xx, 5xx 錯誤響應：使用 warn/error 級別
                        logger.warn("📤 [{}] Response (Status {}): {}", requestId, statusCode, responseContent);
                    }
                }

                // 如果是 ApiResponse，額外記錄結構化資訊
                if (result != null && isApiResponse(result)) {
                    logApiResponseDetails(result, requestId, statusCode);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to log response for {}: {}", joinPoint.getSignature().toShortString(),
                SensitiveDataMasker.mask(e.getMessage()));
        }
    }

    String getRequestParameters(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 0) return "[]";

        // 以「參數名=值」記錄，讓 refreshToken 這類不透明 token 也能依名稱遮罩
        String[] names = joinPoint.getSignature() instanceof MethodSignature sig ? sig.getParameterNames() : null;
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) out.append(", ");
            if (names != null && i < names.length) out.append(names[i]).append('=');
            out.append(describeArg(args[i]));
        }
        return SensitiveDataMasker.mask(out.append(']').toString());
    }

    private String describeArg(Object arg) {
        if (arg == null) return "null";
        String className = arg.getClass().getSimpleName();
        // 不記錄 HttpServletRequest、HttpServletResponse 等物件內容
        if (className.contains("HttpServlet") || className.contains("Request") || className.contains("Response")) {
            return "[" + className + "]";
        }
        return arg.toString();
    }

    private String getRequestHeaders(HttpServletRequest request) {
        // 只記錄重要的請求頭，避免記錄敏感資訊
        StringBuilder headers = new StringBuilder();
        headers.append("User-Agent: ").append(request.getHeader("User-Agent")).append(", ");
        headers.append("Content-Type: ").append(request.getHeader("Content-Type")).append(", ");
        headers.append("Content-Length: ").append(request.getContentLength());
        return headers.toString();
    }

    String truncateResponse(Object result, int statusCode) {
        try {
            String json = objectMapper.writeValueAsString(result);

            // 根據 HTTP 狀態碼決定截斷長度
            int maxLength;
            if (statusCode >= 200 && statusCode < 300) {
                // 2xx 成功響應：只顯示前 2000 字元
                maxLength = 2000;
            } else {
                // 3xx, 4xx, 5xx 錯誤響應：顯示完整內容
                maxLength = Integer.MAX_VALUE;
            }

            json = SensitiveDataMasker.mask(json);
            if (json.length() > maxLength) {
                return json.substring(0, maxLength) + "... [truncated, status: " + statusCode + "]";
            }
            return json;
        } catch (Exception e) {
            return result.getClass().getSimpleName() + " [cannot serialize]";
        }
    }

    /**
     * 檢查對象是否為 ApiResponse 類型
     */
    private boolean isApiResponse(Object result) {
        if (result == null) return false;
        String className = result.getClass().getSimpleName();
        return className.contains("ApiResponse") || className.contains("Response");
    }

    /**
     * 記錄 ApiResponse 的詳細資訊
     */
    void logApiResponseDetails(Object result, String requestId, int statusCode) {
        try {
            // 使用反射來檢查 ApiResponse 的屬性
            Class<?> clazz = result.getClass();

            // 檢查是否包含 success, code, message 屬性
            boolean hasSuccess = hasField(clazz, "success");
            boolean hasCode = hasField(clazz, "code");
            boolean hasMessage = hasField(clazz, "message");

            if (hasSuccess && hasCode && hasMessage) {
                // 這是一個標準的 ApiResponse
                Object success = getFieldValue(result, "success");
                Object code = getFieldValue(result, "code");
                String message = SensitiveDataMasker.mask(String.valueOf(getFieldValue(result, "message")));

                if (statusCode >= 200 && statusCode < 300) {
                    logger.debug("📊 [{}] ApiResponse - success: {}, code: {}, message: {}",
                        requestId, success, code, message);
                } else {
                    logger.warn("📊 [{}] ApiResponse - success: {}, code: {}, message: {}",
                        requestId, success, code, message);
                }

                // 如果有 error 字段，也記錄下來
                Object error = getFieldValue(result, "error");
                if (error != null && !error.toString().isEmpty()) {
                    logger.warn("🚨 [{}] ApiResponse error: {}", requestId, SensitiveDataMasker.mask(error.toString()));
                }
            }
        } catch (Exception e) {
            // 如果反射失敗，靜默忽略，不影響主要日誌功能
            logger.trace("Failed to extract ApiResponse details: {}", e.getMessage());
        }
    }

    private boolean hasField(Class<?> clazz, String fieldName) {
        try {
            clazz.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private Object getFieldValue(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
