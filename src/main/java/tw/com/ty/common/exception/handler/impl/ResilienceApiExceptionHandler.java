package tw.com.ty.common.exception.handler.impl;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import tw.com.ty.common.response.ErrorCode;
import tw.com.ty.common.response.ErrorResponse;
import tw.com.ty.common.exception.handler.ApiExceptionHandler;

/**
 * 彈性異常處理器（限流、熔斷等）
 */
@Component
@Order(3)
public class ResilienceApiExceptionHandler implements ApiExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        // 處理各種彈性相關異常
        return ex.getMessage() != null && (
            ex.getMessage().contains("rate limit") ||
            ex.getMessage().contains("circuit breaker") ||
            ex.getMessage().contains("bulkhead") ||
            ex.getMessage().contains("timeout")
        );
    }

    @Override
    public ErrorResponse handle(Exception ex, String requestUri) {
        ErrorCode errorCode = determineErrorCode(ex);
        return ErrorResponse.fromErrorCode(
            errorCode, ex.getMessage(), requestUri);
    }

    private ErrorCode determineErrorCode(Exception ex) {
        String message = ex.getMessage().toLowerCase();
        if (message.contains("rate limit")) {
            return ErrorCode.RATE_LIMIT_EXCEEDED;
        } else if (message.contains("timeout")) {
            return ErrorCode.EXTERNAL_SERVICE_TIMEOUT;
        } else if (message.contains("circuit breaker") || message.contains("bulkhead")) {
            return ErrorCode.BULKHEAD_FULL;
        } else {
            return ErrorCode.EXTERNAL_SERVICE_ERROR;
        }
    }
}
