package tw.com.ty.common.exception;

/**
 * 彈性相關異常類別
 *
 * 用於處理系統彈性相關的異常，如 Rate Limiting、熔斷器等
 */
public class ResilienceException extends BusinessException {

    /**
     * 創建 Rate Limit 超過異常
     */
    public static ResilienceException rateLimitExceeded() {
        return new ResilienceException(ErrorCode.RATE_LIMIT_EXCEEDED);
    }

    /**
     * 創建 Rate Limit 超過異常（帶自訂訊息）
     */
    public static ResilienceException rateLimitExceeded(String message) {
        return new ResilienceException(ErrorCode.RATE_LIMIT_EXCEEDED, message);
    }

    /**
     * 創建通用彈性異常
     */
    public ResilienceException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 創建通用彈性異常（帶自訂訊息）
     */
    public ResilienceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 創建通用彈性異常（帶自訂訊息和原因）
     */
    public ResilienceException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
