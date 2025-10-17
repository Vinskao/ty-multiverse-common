package tw.com.ty.common.exception.handler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.com.ty.common.exception.handler.impl.*;

import java.util.List;

/**
 * 異常處理器工廠
 *
 * 負責建立和管理異常處理器鏈
 */
@Configuration
public class ExceptionHandlerFactory {

    /**
     * 建立 HTTP 異常處理器鏈
     */
    @Bean("httpExceptionHandlers")
    public List<ApiExceptionHandler> createHttpExceptionHandlers() {
        return List.of(
            new BusinessApiExceptionHandler(),
            new ValidationApiExceptionHandler(),
            new DataIntegrityApiExceptionHandler(),
            new ResilienceApiExceptionHandler(),
            new DefaultApiExceptionHandler()
        );
    }

    /**
     * 建立 gRPC 異常處理器鏈
     */
    @Bean("grpcExceptionHandlers")
    public List<GrpcExceptionHandler> createGrpcExceptionHandlers() {
        return List.of(
            new BusinessGrpcExceptionHandler(),
            new ValidationGrpcExceptionHandler(),
            new DefaultGrpcExceptionHandler()
        );
    }
}
