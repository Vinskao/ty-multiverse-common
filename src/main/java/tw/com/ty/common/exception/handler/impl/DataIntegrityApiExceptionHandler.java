package tw.com.ty.common.exception.handler.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tw.com.ty.common.exception.ErrorCode;
import tw.com.ty.common.exception.ErrorResponse;
import tw.com.ty.common.exception.handler.ApiExceptionHandler;

/**
 * 資料完整性異常處理器
 */
@Component
@Order(2)
public class DataIntegrityApiExceptionHandler implements ApiExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return ex instanceof DataIntegrityViolationException;
    }

    @Override
    public ResponseEntity<ErrorResponse> handle(Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
            ErrorCode.DUPLICATE_ENTRY, "資料衝突或約束違反", request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
}
