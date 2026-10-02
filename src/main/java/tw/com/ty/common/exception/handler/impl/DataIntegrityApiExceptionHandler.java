package tw.com.ty.common.exception.handler.impl;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import tw.com.ty.common.response.ErrorCode;
import tw.com.ty.common.response.ErrorResponse;
import tw.com.ty.common.exception.handler.ApiExceptionHandler;

/**
 * 資料完整性異常處理器
 */
@Component
@Order(2)
public class DataIntegrityApiExceptionHandler implements ApiExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return ThrowableTypes.isInstanceOf(ex, "org.springframework.dao.DataIntegrityViolationException");
    }

    @Override
    public ErrorResponse handle(Exception ex, String requestUri) {
        return ErrorResponse.fromErrorCode(
            ErrorCode.DUPLICATE_ENTRY, "資料衝突或約束違反", requestUri);
    }

    @Override
    public org.springframework.http.HttpStatus status(Exception ex) {
        return ErrorCode.DUPLICATE_ENTRY.getHttpStatus();
    }
}
