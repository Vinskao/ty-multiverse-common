package tw.com.ty.common.exception.web;

import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import tw.com.ty.common.exception.translate.ExceptionTranslator;
import tw.com.ty.common.exception.translate.Translation;
import tw.com.ty.common.response.ErrorResponse;

/**
 * Spring WebFlux adapter of the shared error handling. Registered by
 * {@link CommonExceptionAutoConfiguration}; the body is an {@link ErrorResponse}.
 */
@RestControllerAdvice
public class ReactiveExceptionAdvice {

    private final ExceptionTranslator translator;

    public ReactiveExceptionAdvice(ExceptionTranslator translator) {
        this.translator = translator;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception ex, ServerHttpRequest request) {
        Translation t = translator.translate(ex, request.getPath().value());
        return ResponseEntity.status(t.status()).body(t.body());
    }
}
