package tw.com.ty.common.exception.translate;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import tw.com.ty.common.exception.handler.ApiExceptionHandler;
import tw.com.ty.common.exception.handler.impl.BusinessApiExceptionHandler;
import tw.com.ty.common.exception.handler.impl.DataIntegrityApiExceptionHandler;
import tw.com.ty.common.exception.handler.impl.DefaultApiExceptionHandler;
import tw.com.ty.common.exception.handler.impl.JdkExceptionApiExceptionHandler;
import tw.com.ty.common.exception.handler.impl.OptimisticLockApiExceptionHandler;
import tw.com.ty.common.exception.handler.impl.ResilienceApiExceptionHandler;
import tw.com.ty.common.exception.handler.impl.SecurityApiExceptionHandler;
import tw.com.ty.common.exception.handler.impl.SpringWebApiExceptionHandler;
import tw.com.ty.common.exception.handler.impl.ValidationApiExceptionHandler;
import tw.com.ty.common.response.ErrorResponse;
import tw.com.ty.common.security.mask.MaskedException;
import tw.com.ty.common.security.mask.SensitiveDataMasker;

/**
 * Framework-neutral core of the shared error handling: runs an exception through a chain of
 * {@link ApiExceptionHandler}s (first match wins) and returns the HTTP status together with the error
 * payload. The Servlet and WebFlux advices are thin adapters around this class.
 */
public class ExceptionTranslator {

    private static final Logger log = LoggerFactory.getLogger(ExceptionTranslator.class);

    private final List<ApiExceptionHandler> chain;
    private final boolean exposeServerErrorDetail;

    public ExceptionTranslator(List<ApiExceptionHandler> chain) {
        this(chain, false);
    }

    /**
     * @param exposeServerErrorDetail when {@code false} (the default, required in production) a 5xx response only
     *        carries a generic message plus an {@code errorId} that matches the server log entry; when {@code true}
     *        (local development) the masked exception message is returned as well.
     */
    public ExceptionTranslator(List<ApiExceptionHandler> chain, boolean exposeServerErrorDetail) {
        this.chain = List.copyOf(chain);
        this.exposeServerErrorDetail = exposeServerErrorDetail;
    }

    /** The standard chain with server-error details hidden. */
    public static ExceptionTranslator withDefaults() {
        return withDefaults(false);
    }

    /** The standard chain. Order matters: specific first, rule-of-thumb handlers last, default at the end. */
    public static ExceptionTranslator withDefaults(boolean exposeServerErrorDetail) {
        return new ExceptionTranslator(List.of(
                new BusinessApiExceptionHandler(),
                new SecurityApiExceptionHandler(),
                new ValidationApiExceptionHandler(),
                new SpringWebApiExceptionHandler(),
                new DataIntegrityApiExceptionHandler(),
                new OptimisticLockApiExceptionHandler(),
                new JdkExceptionApiExceptionHandler(),
                new ResilienceApiExceptionHandler(),
                new DefaultApiExceptionHandler()), exposeServerErrorDetail);
    }

    public Translation translate(Throwable throwable, String path) {
        Exception ex = throwable instanceof Exception e ? e
                : new RuntimeException(String.valueOf(throwable.getMessage()), throwable);
        ApiExceptionHandler handler = chain.stream().filter(h -> h.canHandle(ex)).findFirst()
                .orElseGet(DefaultApiExceptionHandler::new);
        HttpStatus status = handler.status(ex);
        ErrorResponse raw = handler.handle(ex, path);
        String errorId = UUID.randomUUID().toString().substring(0, 8);

        String detail;
        if (status.is5xxServerError()) {
            detail = exposeServerErrorDetail
                    ? SensitiveDataMasker.mask(raw.getDetail()) + " (errorId=" + errorId + ")"
                    : "伺服器內部錯誤，請將 errorId=" + errorId + " 提供給管理員";
            log.error("[errorId={}] {} -> {}: {}", errorId, path, status.value(),
                    SensitiveDataMasker.mask(String.valueOf(throwable)), MaskedException.of(throwable));
        } else {
            detail = SensitiveDataMasker.mask(raw.getDetail());
            log.warn("[errorId={}] {} -> {}: {}", errorId, path, status.value(),
                    SensitiveDataMasker.mask(String.valueOf(throwable)));
        }
        return new Translation(status,
                new ErrorResponse(raw.getCode(), SensitiveDataMasker.mask(raw.getMessage()), detail, path));
    }
}
