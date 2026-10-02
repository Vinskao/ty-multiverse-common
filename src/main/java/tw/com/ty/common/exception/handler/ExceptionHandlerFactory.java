package tw.com.ty.common.exception.handler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.com.ty.common.exception.handler.impl.*;

import java.util.List;

/**
 * 異常處理器工廠
 *
 * 負責建立和管理異常處理器鏈
 *
 * @deprecated Since 2.3.0: Replaced by ExceptionTranslator.withDefaults(). Not registered by the auto-configuration; use
 *             ExceptionTranslator from {@code tw.com.ty.common.exception} instead. Will be removed in the next major version.
 */
@Deprecated(since = "2.3.0", forRemoval = true)
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

}
