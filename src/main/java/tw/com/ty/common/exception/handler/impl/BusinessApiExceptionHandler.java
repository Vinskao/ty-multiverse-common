package tw.com.ty.common.exception.handler.impl;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tw.com.ty.common.exception.BusinessException;
import tw.com.ty.common.response.ErrorResponse;
import tw.com.ty.common.exception.UnifiedErrorConverter;
import tw.com.ty.common.exception.handler.AbstractApiExceptionHandler;

/**
 * 業務異常處理器 - 支援多協議架構
 */
@Component
@Order(0)
public class BusinessApiExceptionHandler extends AbstractApiExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return ex instanceof BusinessException;
    }

    @Override
    protected ErrorResponse createErrorResponse(BusinessException ex, String requestUri) {
        return UnifiedErrorConverter.toHttpResponse(ex, requestUri);
    }
}

/**
 * WebFlux 專用業務異常處理器範例
 *
 * 此類示範如何在響應式環境中使用統一的異常處理邏輯
 * 注意：此類不需要 @Component，因為每個專案應該有自己的實現
 */
class WebFluxBusinessExceptionHandler extends AbstractApiExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return ex instanceof BusinessException;
    }

    @Override
    protected ErrorResponse createErrorResponse(BusinessException ex, String requestUri) {
        return UnifiedErrorConverter.toHttpResponse(ex, requestUri);
    }

    /**
     * 響應式環境專用方法
     */
    public reactor.core.publisher.Mono<org.springframework.http.ResponseEntity<ErrorResponse>> handleReactive(BusinessException ex, String requestUri) {
        ErrorResponse errorResponse = createErrorResponse(ex, requestUri);
        return reactor.core.publisher.Mono.just(
            new org.springframework.http.ResponseEntity<>(errorResponse, ex.getErrorCode().getHttpStatus())
        );
    }
}
