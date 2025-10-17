package tw.com.ty.common.exception.handler.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tw.com.ty.common.exception.ErrorCode;
import tw.com.ty.common.exception.ErrorResponse;
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
    public ResponseEntity<ErrorResponse> handle(Exception ex, HttpServletRequest request) {
        ErrorCode errorCode = determineErrorCode(ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
            errorCode, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
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
