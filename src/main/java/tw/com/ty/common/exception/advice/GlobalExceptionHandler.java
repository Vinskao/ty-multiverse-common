package tw.com.ty.common.exception.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import tw.com.ty.common.exception.BusinessException;
import tw.com.ty.common.exception.ErrorCode;
import tw.com.ty.common.exception.ErrorResponse;
import tw.com.ty.common.exception.UnifiedErrorConverter;
import tw.com.ty.common.exception.handler.ApiExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 全域異常處理器 - 專為 Web MVC 設計
 *
 * 統一處理應用程序中的各種異常，並返回標準化的錯誤響應。
 * 注意：此類依賴於 Servlet API，只能在 Web MVC 環境中使用。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final List<ApiExceptionHandler> handlerChain;

    @Autowired
    public GlobalExceptionHandler(List<ApiExceptionHandler> handlerChain) {
        this.handlerChain = handlerChain;
    }

    /**
     * 處理業務異常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        logger.error("業務異常: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = UnifiedErrorConverter.toHttpResponse(ex, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ex.getErrorCode().getHttpStatus());
    }


    /**
     * 使用責任鏈模式處理所有異常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, HttpServletRequest request) {
        for (ApiExceptionHandler handler : handlerChain) {
            if (handler.canHandle(ex)) {
                ErrorResponse errorResponse = handler.handle(ex, request.getRequestURI());
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // 預設處理
        logger.error("未處理的異常: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
