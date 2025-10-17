package tw.com.ty.common.exception.handler.impl;

import io.grpc.Status;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tw.com.ty.common.exception.ErrorCode;
import tw.com.ty.common.exception.UnifiedErrorConverter;
import tw.com.ty.common.exception.handler.GrpcExceptionHandler;

/**
 * gRPC 預設異常處理器
 */
@Component
@Order(Integer.MAX_VALUE)
public class DefaultGrpcExceptionHandler implements GrpcExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return true; // 處理所有異常
    }

    @Override
    public Status handle(Exception ex) {
        return UnifiedErrorConverter.convertErrorCodeToGrpcStatus(ErrorCode.INTERNAL_SERVER_ERROR)
                .withDescription(ex.getMessage());
    }
}
