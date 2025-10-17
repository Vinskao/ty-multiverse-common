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
    public ResponseEntity<ErrorResponse> handle(Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
            ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
