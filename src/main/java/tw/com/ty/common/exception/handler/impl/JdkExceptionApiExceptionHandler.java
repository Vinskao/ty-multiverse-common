package tw.com.ty.common.exception.handler.impl;

import org.springframework.http.HttpStatus;

import tw.com.ty.common.exception.handler.ApiExceptionHandler;
import tw.com.ty.common.response.ErrorCode;
import tw.com.ty.common.response.ErrorResponse;

/**
 * Standard JDK exceptions that carry a clear meaning (same rules as {@code UnifiedErrorConverter}):
 * {@link IllegalArgumentException} → 400, {@link SecurityException} → 403,
 * {@link UnsupportedOperationException} → 400 (INVALID_OPERATION).
 */
public class JdkExceptionApiExceptionHandler implements ApiExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return ex instanceof IllegalArgumentException
                || ex instanceof SecurityException
                || ex instanceof UnsupportedOperationException;
    }

    @Override
    public ErrorResponse handle(Exception ex, String requestUri) {
        return ErrorResponse.fromErrorCode(errorCode(ex), ex.getMessage(), requestUri);
    }

    @Override
    public HttpStatus status(Exception ex) {
        return errorCode(ex).getHttpStatus();
    }

    private ErrorCode errorCode(Exception ex) {
        if (ex instanceof SecurityException) {
            return ErrorCode.FORBIDDEN;
        }
        if (ex instanceof UnsupportedOperationException) {
            return ErrorCode.INVALID_OPERATION;
        }
        return ErrorCode.BAD_REQUEST;
    }
}
