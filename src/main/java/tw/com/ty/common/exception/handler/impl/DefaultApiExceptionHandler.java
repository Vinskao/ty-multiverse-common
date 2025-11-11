package tw.com.ty.common.exception.handler.impl;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import tw.com.ty.common.response.ErrorCode;
import tw.com.ty.common.response.ErrorResponse;
import tw.com.ty.common.exception.handler.ApiExceptionHandler;

/**
 * 預設異常處理器（處理所有未被其他處理器處理的異常）
 */
@Component
@Order(Integer.MAX_VALUE)
public class DefaultApiExceptionHandler implements ApiExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return true; // 處理所有異常
    }

    @Override
    public ErrorResponse handle(Exception ex, String requestUri) {
        return ErrorResponse.fromErrorCode(
            ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage(), requestUri);
    }
}
