package tw.com.ty.common.exception.handler.impl;

import org.springframework.http.HttpStatus;

import tw.com.ty.common.exception.handler.ApiExceptionHandler;
import tw.com.ty.common.response.ErrorCode;
import tw.com.ty.common.response.ErrorResponse;

/** Optimistic locking failures (JPA / Spring DAO) → 409. */
public class OptimisticLockApiExceptionHandler implements ApiExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return ThrowableTypes.isInstanceOf(ex,
                "org.springframework.dao.OptimisticLockingFailureException",
                "org.hibernate.StaleObjectStateException",
                "jakarta.persistence.OptimisticLockException");
    }

    @Override
    public ErrorResponse handle(Exception ex, String requestUri) {
        return ErrorResponse.fromErrorCode(ErrorCode.OPTIMISTIC_LOCKING_FAILURE, "資料已被其他使用者修改", requestUri);
    }

    @Override
    public HttpStatus status(Exception ex) {
        return ErrorCode.OPTIMISTIC_LOCKING_FAILURE.getHttpStatus();
    }
}
