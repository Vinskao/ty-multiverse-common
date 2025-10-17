package tw.com.ty.common.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import tw.com.ty.common.exception.ErrorResponse;

/**
 * HTTP API 異常處理器介面
 *
 * 使用責任鏈模式處理不同類型的異常
 */
public interface ApiExceptionHandler {

    /**
     * 判斷是否能處理該異常
     */
    boolean canHandle(Exception ex);

    /**
     * 處理異常並返回 HTTP 響應
     */
    ResponseEntity<ErrorResponse> handle(Exception ex, HttpServletRequest request);
}
