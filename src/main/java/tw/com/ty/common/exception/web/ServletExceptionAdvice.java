package tw.com.ty.common.exception.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import tw.com.ty.common.exception.translate.ExceptionTranslator;
import tw.com.ty.common.exception.translate.Translation;
import tw.com.ty.common.response.BackendApiResponse;
import tw.com.ty.common.response.ErrorResponse;

/**
 * Spring MVC (servlet) adapter of the shared error handling. Registered by
 * {@link CommonExceptionAutoConfiguration}; the body is a {@link BackendApiResponse} so errors look like
 * the success responses of the same service.
 */
@RestControllerAdvice
public class ServletExceptionAdvice {

    private final ExceptionTranslator translator;

    public ServletExceptionAdvice(ExceptionTranslator translator) {
        this.translator = translator;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BackendApiResponse<Void>> handle(Exception ex, HttpServletRequest request) {
        Translation t = translator.translate(ex, request.getRequestURI());
        ErrorResponse error = t.body();
        BackendApiResponse<Void> body = BackendApiResponse.error(error.getCode(), error.getMessage());
        body.setError(error.getDetail());
        return ResponseEntity.status(t.status()).body(body);
    }
}
