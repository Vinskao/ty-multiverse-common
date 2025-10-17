package tw.com.ty.common.exception;

import io.grpc.Status;
import org.springframework.http.HttpStatus;

/**
 * 統一錯誤轉換器
 *
 * 負責將業務異常統一轉換為不同協議（HTTP/gRPC）的錯誤格式
 * 實現雙協議錯誤處理的橋接層
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
     * 將異常轉換為 gRPC Status
     */
    public static Status toGrpcStatus(BusinessException exception) {
        return convertErrorCodeToGrpcStatus(exception.getErrorCode())
                .withDescription(exception.getMessage());
    }

    /**
     * 將異常轉換為 gRPC Status（帶詳細訊息）
     */
    public static Status toGrpcStatus(BusinessException exception, String detail) {
        return convertErrorCodeToGrpcStatus(exception.getErrorCode())
                .withDescription(detail != null ? detail : exception.getMessage());
    }

    /**
     * 將錯誤代碼轉換為 gRPC Status Code
     */
    public static Status convertErrorCodeToGrpcStatus(ErrorCode errorCode) {
        switch (errorCode) {
            case NOT_FOUND:
            case ENTITY_NOT_FOUND:
                return Status.NOT_FOUND;

            case BAD_REQUEST:
            case INVALID_OPERATION:
            case BUSINESS_RULE_VIOLATION:
            case INVALID_FILE_FORMAT:
            case FILE_TOO_LARGE:
                return Status.INVALID_ARGUMENT;

            case UNAUTHORIZED:
            case TOKEN_EXPIRED:
            case TOKEN_INVALID:
            case TOKEN_MISSING:
            case AUTHENTICATION_FAILED:
                return Status.UNAUTHENTICATED;

            case FORBIDDEN:
            case INSUFFICIENT_PERMISSIONS:
            case AUTHORIZATION_FAILED:
                return Status.PERMISSION_DENIED;

            case EXTERNAL_SERVICE_ERROR:
            case EXTERNAL_SERVICE_TIMEOUT:
                return Status.UNAVAILABLE;

            case RATE_LIMIT_EXCEEDED:
            case BULKHEAD_FULL:
                return Status.RESOURCE_EXHAUSTED;

            case CONFLICT:
            case DUPLICATE_ENTRY:
            case OPTIMISTIC_LOCKING_FAILURE:
                return Status.ALREADY_EXISTS;

            case FILE_NOT_FOUND:
            case FILE_UPLOAD_ERROR:
            case FILE_DOWNLOAD_ERROR:
                return Status.NOT_FOUND;

            case TOKEN_INTROSPECT_FAILED:
            case TOKEN_REFRESH_FAILED:
            case TOKEN_INVALID_OR_REFRESH_FAILED:
            case TOKEN_CHECK_FAILED:
                return Status.UNAUTHENTICATED;

            case SESSION_EXPIRED:
            case SESSION_INVALID:
            case SESSION_NOT_FOUND:
                return Status.UNAUTHENTICATED;

            case USER_NOT_LOGGED_IN:
            case NO_ACTIVE_GAME:
                return Status.FAILED_PRECONDITION;

            case CSRF_DISABLED:
            case CORS_ENABLED:
                return Status.OK; // 配置相關，非錯誤

            case LOGOUT_SUCCESS:
                return Status.OK;

            case LOGOUT_FAILED:
                return Status.INTERNAL;

            default:
                return Status.INTERNAL;
        }
    }

    /**
     * 將 gRPC Status 轉換為錯誤代碼（反向轉換）
     */
    public static ErrorCode fromGrpcStatus(Status status) {
        switch (status.getCode()) {
            case NOT_FOUND:
                return ErrorCode.ENTITY_NOT_FOUND;
            case INVALID_ARGUMENT:
                return ErrorCode.BAD_REQUEST;
            case UNAUTHENTICATED:
                return ErrorCode.UNAUTHORIZED;
            case PERMISSION_DENIED:
                return ErrorCode.FORBIDDEN;
            case UNAVAILABLE:
                return ErrorCode.EXTERNAL_SERVICE_ERROR;
            case RESOURCE_EXHAUSTED:
                return ErrorCode.RATE_LIMIT_EXCEEDED;
            case ALREADY_EXISTS:
                return ErrorCode.DUPLICATE_ENTRY;
            case FAILED_PRECONDITION:
                return ErrorCode.BUSINESS_RULE_VIOLATION;
            case UNIMPLEMENTED:
                return ErrorCode.INVALID_OPERATION;
            default:
                return ErrorCode.INTERNAL_SERVER_ERROR;
        }
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
