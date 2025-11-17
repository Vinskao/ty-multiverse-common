package tw.com.ty.common.exception.handler.impl;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

import tw.com.ty.common.response.ErrorCode;
import tw.com.ty.common.response.ErrorResponse;
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
    public ErrorResponse handle(Exception ex, String requestUri) {
        MethodArgumentNotValidException validationEx = (MethodArgumentNotValidException) ex;
        String detail = validationEx.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("參數驗證失敗");

        return ErrorResponse.fromErrorCode(
            ErrorCode.BAD_REQUEST, detail, requestUri);
    }
}
