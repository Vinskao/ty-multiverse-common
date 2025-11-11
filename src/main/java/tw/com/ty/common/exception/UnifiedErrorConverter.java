package tw.com.ty.common.exception;

import tw.com.ty.common.response.ErrorCode;
import tw.com.ty.common.response.ErrorResponse;

/**
 * 統一錯誤轉換器
 *
 * 負責將業務異常統一轉換為 HTTP 錯誤格式
 */
public class UnifiedErrorConverter {

    /**
     * 將異常轉換為 HTTP 錯誤響應
     */
    public static ErrorResponse toHttpResponse(BusinessException exception, String path) {
        return ErrorResponse.fromBusinessException(exception, path);
    }

    /**
     * 將異常轉換為 HTTP 錯誤響應（靜態工廠方法）
     */
    public static ErrorResponse toHttpResponse(BusinessException exception, String path, String detail) {
        return ErrorResponse.fromErrorCode(exception.getErrorCode(), detail, path);
    }


    /**
     * 將一般異常轉換為業務異常
     */
    public static BusinessException convertToBusinessException(Exception exception) {
        if (exception instanceof BusinessException) {
            return (BusinessException) exception;
        }

        // 根據異常類型轉換
        if (exception instanceof IllegalArgumentException) {
            return new BusinessException(ErrorCode.BAD_REQUEST, exception.getMessage(), exception);
        } else if (exception instanceof SecurityException) {
            return new BusinessException(ErrorCode.FORBIDDEN, exception.getMessage(), exception);
        } else if (exception instanceof UnsupportedOperationException) {
            return new BusinessException(ErrorCode.INVALID_OPERATION, exception.getMessage(), exception);
        } else {
            return new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
        }
    }
}
