package tw.com.ty.common.exception.handler;

import tw.com.ty.common.exception.ErrorResponse;

/**
 * 抽象異常處理器基類
 *
 * 提供通用的異常處理邏輯，所有具體的異常處理器都應該繼承此類
 */
public abstract class AbstractApiExceptionHandler implements ApiExceptionHandler {

    @Override
    public final ErrorResponse handle(Exception ex, String requestUri) {
        // 將異常轉換為業務異常
        var businessException = convertToBusinessException(ex);

        // 創建錯誤響應
        return createErrorResponse(businessException, requestUri);
    }

    /**
     * 將一般異常轉換為業務異常
     * 子類可以覆寫此方法來自定義轉換邏輯
     */
    protected tw.com.ty.common.exception.BusinessException convertToBusinessException(Exception ex) {
        if (ex instanceof tw.com.ty.common.exception.BusinessException) {
            return (tw.com.ty.common.exception.BusinessException) ex;
        }
        // 預設轉換邏輯
        return new tw.com.ty.common.exception.BusinessException(
            tw.com.ty.common.exception.ErrorCode.INTERNAL_SERVER_ERROR,
            "系統異常：" + ex.getMessage(),
            ex
        );
    }

    /**
     * 創建錯誤響應
     * 子類可以覆寫此方法來自定義響應格式
     */
    protected ErrorResponse createErrorResponse(tw.com.ty.common.exception.BusinessException ex, String requestUri) {
        return tw.com.ty.common.exception.ErrorResponse.fromErrorCode(
            ex.getErrorCode(),
            ex.getMessage(),
            requestUri
        );
    }
}

