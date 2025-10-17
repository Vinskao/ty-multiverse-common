package tw.com.ty.common.exception.handler;

import io.grpc.Status;
import tw.com.ty.common.exception.BusinessException;

/**
 * gRPC 異常處理器介面
 *
 * 使用責任鏈模式處理不同類型的異常並轉換為 gRPC Status
 */
public interface GrpcExceptionHandler {

    /**
     * 判斷是否能處理該異常
     */
    boolean canHandle(Exception ex);

    /**
     * 處理異常並返回 gRPC Status
     */
    Status handle(Exception ex);
}
