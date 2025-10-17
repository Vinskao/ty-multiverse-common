package tw.com.ty.common.exception.advice;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tw.com.ty.common.exception.BusinessException;
import tw.com.ty.common.exception.ErrorCode;
import tw.com.ty.common.exception.UnifiedErrorConverter;
import tw.com.ty.common.exception.handler.GrpcExceptionHandler;

import java.util.List;

/**
 * gRPC 全域異常攔截器
 *
 * 統一處理 gRPC 服務中的異常，並轉換為標準化的 gRPC Status
 */
@Component
public class GrpcGlobalInterceptor implements ServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(GrpcGlobalInterceptor.class);

    private final List<GrpcExceptionHandler> handlerChain;

    @Autowired
    public GrpcGlobalInterceptor(List<GrpcExceptionHandler> handlerChain) {
        this.handlerChain = handlerChain;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(call, headers)) {

            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Exception e) {
                    logger.error("❌ gRPC Error in {}: {}", call.getMethodDescriptor().getFullMethodName(), e.getMessage(), e);

                    // 使用責任鏈處理異常
                    Status status = handleException(e);
                    call.close(status, new Metadata());
                }
            }

            @Override
            public void onCancel() {
                logger.warn("⚠️ gRPC Cancelled: {}", call.getMethodDescriptor().getFullMethodName());
                super.onCancel();
            }

            private Status handleException(Exception e) {
                for (GrpcExceptionHandler handler : handlerChain) {
                    if (handler.canHandle(e)) {
                        return handler.handle(e);
                    }
                }

                // 預設處理
                return UnifiedErrorConverter.convertErrorCodeToGrpcStatus(ErrorCode.INTERNAL_SERVER_ERROR)
                        .withDescription(e.getMessage());
            }
        };
    }
}
