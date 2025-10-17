package tw.com.ty.common.exception.handler.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tw.com.ty.common.exception.BusinessException;
import tw.com.ty.common.exception.ErrorResponse;
import tw.com.ty.common.exception.UnifiedErrorConverter;
import tw.com.ty.common.exception.handler.ApiExceptionHandler;

/**
 * 業務異常處理器
 */
@Component
@Order(0)
public class BusinessApiExceptionHandler implements ApiExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return ex instanceof BusinessException;
    }

    @Override
    public ResponseEntity<ErrorResponse> handle(Exception ex, HttpServletRequest request) {
        BusinessException be = (BusinessException) ex;
        ErrorResponse response = UnifiedErrorConverter.toHttpResponse(be, request.getRequestURI());
        return new ResponseEntity<>(response, be.getErrorCode().getHttpStatus());
    }
}
