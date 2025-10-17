package tw.com.ty.common.exception.handler.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import tw.com.ty.common.exception.ErrorCode;
import tw.com.ty.common.exception.ErrorResponse;
import tw.com.ty.common.exception.handler.ApiExceptionHandler;

/**
 * 驗證異常處理器
 */
@Component
@Order(1)
public class ValidationApiExceptionHandler implements ApiExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return ex instanceof MethodArgumentNotValidException;
    }

    @Override
    public ResponseEntity<ErrorResponse> handle(Exception ex, HttpServletRequest request) {
        MethodArgumentNotValidException validationEx = (MethodArgumentNotValidException) ex;
        String detail = validationEx.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("參數驗證失敗");

        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
            ErrorCode.BAD_REQUEST, detail, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
