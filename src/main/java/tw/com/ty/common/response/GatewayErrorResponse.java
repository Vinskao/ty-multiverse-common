package tw.com.ty.common.response;

import lombok.Getter;
import tw.com.ty.common.exception.BusinessException;

import java.time.LocalDateTime;

/**
 * Gateway 錯誤響應類 (響應式版本)
 * 用於 Spring Cloud Gateway 的錯誤響應格式
 */
@Getter
public class GatewayErrorResponse extends BaseErrorResponse {

    public GatewayErrorResponse(int code, String message, String detail, LocalDateTime timestamp, String path) {
        super(code, message, detail, timestamp, path);
    }

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
