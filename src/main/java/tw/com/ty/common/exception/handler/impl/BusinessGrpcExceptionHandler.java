package tw.com.ty.common.exception.handler.impl;

import io.grpc.Status;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tw.com.ty.common.exception.BusinessException;
import tw.com.ty.common.exception.UnifiedErrorConverter;
import tw.com.ty.common.exception.handler.GrpcExceptionHandler;

/**
 * gRPC 業務異常處理器
 */
@Component
@Order(0)
public class BusinessGrpcExceptionHandler implements GrpcExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return ex instanceof BusinessException;
    }

    @Override
    public Status handle(Exception ex) {
        BusinessException be = (BusinessException) ex;
        return UnifiedErrorConverter.toGrpcStatus(be);
    }
}
