package tw.com.ty.common.security.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import tw.com.ty.common.response.ErrorResponse;
import tw.com.ty.common.response.ErrorCode;

/**
 * 安全異常處理器
 *
 * <p>處理 Spring Security 相關的異常：</p>
 * <ul>
 *   <li>認證異常 (AuthenticationException)</li>
 *   <li>授權異常 (AccessDeniedException)</li>
 *   <li>憑證異常 (BadCredentialsException)</li>
 * </ul>
 *
 * @author TY Backend Team
 * @version 1.0
 * @since 2025
 */
@RestControllerAdvice
public class SecurityExceptionHandler {

    /**
     * 處理認證異常
     *
     * @param ex 認證異常
     * @param request HTTP 請求
     * @return 錯誤響應
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            jakarta.servlet.http.HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
            ErrorCode.UNAUTHORIZED,
            "認證失敗: " + ex.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 處理憑證異常
     *
     * @param ex 憑證異常
     * @param request HTTP 請求
     * @return 錯誤響應
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex,
            jakarta.servlet.http.HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
            ErrorCode.UNAUTHORIZED,
            "用戶名或密碼錯誤",
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 處理授權異常
     *
     * @param ex 授權異常
     * @param request HTTP 請求
     * @return 錯誤響應
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            jakarta.servlet.http.HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
            ErrorCode.FORBIDDEN,
            "權限不足，無法訪問此資源",
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
}
