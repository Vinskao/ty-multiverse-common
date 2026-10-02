package tw.com.ty.common.exception.handler.impl;

import org.springframework.http.HttpStatus;

import tw.com.ty.common.exception.handler.ApiExceptionHandler;
import tw.com.ty.common.response.ErrorCode;
import tw.com.ty.common.response.ErrorResponse;

/** Spring Security {@code AccessDeniedException} → 403, {@code AuthenticationException} → 401. */
public class SecurityApiExceptionHandler implements ApiExceptionHandler {

    private static final String ACCESS_DENIED = "org.springframework.security.access.AccessDeniedException";
    private static final String AUTHENTICATION = "org.springframework.security.core.AuthenticationException";

    @Override
    public boolean canHandle(Exception ex) {
        return ThrowableTypes.isInstanceOf(ex, ACCESS_DENIED, AUTHENTICATION);
    }

    @Override
    public ErrorResponse handle(Exception ex, String requestUri) {
        if (isAccessDenied(ex)) {
            return ErrorResponse.fromErrorCode(ErrorCode.FORBIDDEN, "權限不足，無法訪問此資源", requestUri);
        }
        return ErrorResponse.fromErrorCode(ErrorCode.UNAUTHORIZED, "認證失敗", requestUri);
    }

    @Override
    public HttpStatus status(Exception ex) {
        return isAccessDenied(ex) ? ErrorCode.FORBIDDEN.getHttpStatus() : ErrorCode.UNAUTHORIZED.getHttpStatus();
    }

    private boolean isAccessDenied(Exception ex) {
        return ThrowableTypes.isInstanceOf(ex, ACCESS_DENIED);
    }
}
