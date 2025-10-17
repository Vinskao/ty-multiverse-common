package tw.com.ty.common.exception.handler.impl;

import io.grpc.Status;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import tw.com.ty.common.exception.ErrorCode;
import tw.com.ty.common.exception.UnifiedErrorConverter;
import tw.com.ty.common.exception.handler.GrpcExceptionHandler;

/**
 * gRPC 驗證異常處理器
 */
@Component
@Order(1)
public class ValidationGrpcExceptionHandler implements GrpcExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return ex instanceof MethodArgumentNotValidException;
    }

    @Override
    public Status handle(Exception ex) {
        return UnifiedErrorConverter.convertErrorCodeToGrpcStatus(ErrorCode.BAD_REQUEST)
                .withDescription("參數驗證失敗: " + ex.getMessage());
    }
}
