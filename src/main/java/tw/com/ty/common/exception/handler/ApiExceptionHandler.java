package tw.com.ty.common.exception.handler;

import tw.com.ty.common.response.ErrorResponse;

/**
 * API 異常處理器介面 - 協議無關設計
 *
 * 此接口定義了異常處理的基本契約，不依賴於具體的 web 框架
 * 使用責任鏈模式處理不同類型的異常
 */
public interface ApiExceptionHandler {

    /**
     * 判斷是否可以處理該異常
     */
    boolean canHandle(Exception ex);

    /**
     * 處理異常並返回錯誤響應
     * 注意：具體的響應包裝由實現類決定（ResponseEntity 或 Mono<ResponseEntity>）
     */
    ErrorResponse handle(Exception ex, String requestUri);

    /**
     * HTTP status this handler maps the exception to. Defaults to 500 so existing implementations keep
     * their old behavior; override it so the status is not hard-coded by the web adapter.
     */
    default org.springframework.http.HttpStatus status(Exception ex) {
        return org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
