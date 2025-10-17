package tw.com.ty.common.exception.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tw.com.ty.common.exception.BusinessException;
import tw.com.ty.common.exception.ErrorCode;
import tw.com.ty.common.exception.ErrorResponse;
import tw.com.ty.common.exception.UnifiedErrorConverter;
import tw.com.ty.common.exception.handler.ApiExceptionHandler;

import java.util.List;

/**
 * 全域異常處理器
 *
 * 統一處理應用程序中的各種異常，並返回標準化的錯誤響應。
 * 支援雙協議錯誤處理架構。
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

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
     * 處理方法參數驗證失敗異常
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @SuppressWarnings("null") @NonNull MethodArgumentNotValidException ex,
            @SuppressWarnings("null") @NonNull HttpHeaders headers,
            @SuppressWarnings("null") @NonNull HttpStatusCode status,
            @SuppressWarnings("null") @NonNull WebRequest request) {
        logger.error("方法參數驗證失敗: {}", ex.getMessage(), ex);
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("參數驗證失敗");

        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.BAD_REQUEST, detail, request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 處理文件上傳大小超限異常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        logger.error("文件上傳大小超限: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.FILE_TOO_LARGE, "文件大小超過限制", request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 處理約束違反異常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        logger.error("約束違反: {}", ex.getMessage(), ex);
        String detail = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("約束違反");

        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.BAD_REQUEST, detail, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 使用責任鏈模式處理所有異常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, HttpServletRequest request) {
        for (ApiExceptionHandler handler : handlerChain) {
            if (handler.canHandle(ex)) {
                return handler.handle(ex, request);
            }
        }

        // 預設處理
        logger.error("未處理的異常: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
