package tw.com.ty.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Gateway 錯誤響應類 (響應式版本)
 * 用於 Spring Cloud Gateway 的錯誤響應格式
 */
@Getter
@RequiredArgsConstructor
public class GatewayErrorResponse {

    private final int code;
    private final String message;
    private final String detail;
    private final LocalDateTime timestamp;
    private final String path;

    /**
     * 從錯誤代碼創建錯誤響應
     */
    public static GatewayErrorResponse fromErrorCode(ErrorCode errorCode, String detail, String path) {
        return new GatewayErrorResponse(
            errorCode.getCode(),
            errorCode.getMessage(),
            detail,
            LocalDateTime.now(),
            path
        );
    }

    /**
     * 從業務異常創建錯誤響應
     */
    public static GatewayErrorResponse fromBusinessException(BusinessException exception, String path) {
        return new GatewayErrorResponse(
            exception.getErrorCode().getCode(),
            exception.getErrorCode().getMessage(),
            exception.getMessage(),
            LocalDateTime.now(),
            path
        );
    }
}
