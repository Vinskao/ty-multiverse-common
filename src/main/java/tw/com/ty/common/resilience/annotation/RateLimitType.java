package tw.com.ty.common.resilience.annotation;

/**
 * Rate Limiter 類型枚舉
 */
public enum RateLimitType {
    /**
     * 一般 API 請求
     */
    API,

    /**
     * 批量處理請求
     */
    BATCH
}
