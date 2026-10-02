package tw.com.ty.common.exception.translate;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.server.ResponseStatusException;

import tw.com.ty.common.exception.BusinessException;
import tw.com.ty.common.response.ErrorCode;

class ExceptionTranslatorTest {

    private final ExceptionTranslator translator = ExceptionTranslator.withDefaults();

    private Translation translate(Throwable ex) {
        return translator.translate(ex, "/x");
    }

    @Test
    void translate_Should_UseErrorCodeStatus_When_BusinessException() {
        Translation t = translate(new BusinessException(ErrorCode.WEAPON_NOT_FOUND, "no Sword"));

        assertThat(t.status()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(t.body().getCode()).isEqualTo(ErrorCode.WEAPON_NOT_FOUND.getCode());
        assertThat(t.body().getDetail()).isEqualTo("no Sword");
        assertThat(t.body().getPath()).isEqualTo("/x");
    }

    @Test
    void translate_Should_Return403_When_AccessDenied() {
        Translation t = translate(new AccessDeniedException("nope"));

        assertThat(t.status()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(t.body().getCode()).isEqualTo(ErrorCode.FORBIDDEN.getCode());
    }

    @Test
    void translate_Should_Return401_When_AuthenticationFails() {
        Translation t = translate(new BadCredentialsException("bad"));

        assertThat(t.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(t.body().getCode()).isEqualTo(ErrorCode.UNAUTHORIZED.getCode());
    }

    @Test
    void translate_Should_Return400_When_JsonBodyUnreadable() {
        assertThat(translate(new HttpMessageNotReadableException("bad json")).status())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void translate_Should_Return400_When_RequiredParameterMissing() {
        assertThat(translate(new MissingServletRequestParameterException("name", "String")).status())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void translate_Should_Return400_When_ParameterHasWrongType() throws Exception {
        Method m = getClass().getDeclaredMethod("dummy", String.class);
        var ex = new org.springframework.web.method.annotation.MethodArgumentTypeMismatchException(
                "abc", Integer.class, "minDamage", new MethodParameter(m, 0), new NumberFormatException("abc"));

        assertThat(translate(ex).status()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void translate_Should_Return400_When_ModelAttributeBindingFails() {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "form");
        binding.addError(new org.springframework.validation.FieldError("form", "age", "must be a number"));

        assertThat(translate(new org.springframework.validation.BindException(binding)).status())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void translate_Should_Return400_When_ConstraintViolation() {
        assertThat(translate(new jakarta.validation.ConstraintViolationException("age: must be positive",
                java.util.Set.of())).status()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void translate_Should_Return405_When_MethodNotSupported() {
        assertThat(translate(new HttpRequestMethodNotSupportedException("PATCH")).status())
                .isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void translate_Should_KeepStatus_When_ResponseStatusException() {
        assertThat(translate(new ResponseStatusException(HttpStatus.NOT_FOUND, "gone")).status())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void translate_Should_Return400WithFieldDetail_When_BeanValidationFails() throws Exception {
        Method m = getClass().getDeclaredMethod("dummy", String.class);
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "dto");
        binding.rejectValue(null, "x", "global") ;
        binding.addError(new org.springframework.validation.FieldError("dto", "name", "must not be blank"));

        Translation t = translate(new MethodArgumentNotValidException(new MethodParameter(m, 0), binding));

        assertThat(t.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(t.body().getDetail()).contains("name: must not be blank");
    }

    @Test
    void translate_Should_Return409_When_DataIntegrityViolation() {
        Translation t = translate(new DataIntegrityViolationException("dup"));

        assertThat(t.status()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(t.body().getCode()).isEqualTo(ErrorCode.DUPLICATE_ENTRY.getCode());
    }

    @Test
    void translate_Should_Return409_When_OptimisticLockFails() {
        Translation t = translate(new ObjectOptimisticLockingFailureException(Object.class, "id"));

        assertThat(t.status()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(t.body().getCode()).isEqualTo(ErrorCode.OPTIMISTIC_LOCKING_FAILURE.getCode());
    }

    @Test
    void translate_Should_Return400_When_IllegalArgument() {
        assertThat(translate(new IllegalArgumentException("bad input")).status()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void translate_Should_Return403_When_JdkSecurityException() {
        Translation t = translate(new SecurityException("sandbox"));

        assertThat(t.status()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(t.body().getCode()).isEqualTo(ErrorCode.FORBIDDEN.getCode());
    }

    @Test
    void translate_Should_Return400_When_UnsupportedOperation() {
        Translation t = translate(new UnsupportedOperationException("nope"));

        assertThat(t.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(t.body().getCode()).isEqualTo(ErrorCode.INVALID_OPERATION.getCode());
    }

    @Test
    void translate_Should_NotLetResilienceSniffingHijack_When_IllegalArgumentMentionsTimeout() {
        assertThat(translate(new IllegalArgumentException("timeout must be positive")).status())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void translate_Should_Return429_When_RateLimitMessage() {
        assertThat(translate(new RuntimeException("rate limit exceeded")).status())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void translate_Should_Return500_When_Unknown() {
        Translation t = translate(new IllegalStateException("kaboom"));

        assertThat(t.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(t.body().getCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
    }

    @Test
    void translate_Should_Return500_When_ExceptionHasNullMessage() {
        assertThat(translate(new NullPointerException()).status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void translate_Should_UnwrapNothing_ButHandleNonExceptionThrowable() {
        assertThat(translate(new StackOverflowError()).status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ---- sensitive-data masking ------------------------------------------------------------

    private ListAppender<ILoggingEvent> logs;

    @BeforeEach
    void captureLogs() {
        logs = new ListAppender<>();
        logs.start();
        ((Logger) LoggerFactory.getLogger(ExceptionTranslator.class)).addAppender(logs);
    }

    @AfterEach
    void releaseLogs() {
        ((Logger) LoggerFactory.getLogger(ExceptionTranslator.class)).detachAppender(logs);
    }

    @Test
    void translate_Should_HideServerErrorDetail_AndReturnErrorId() {
        Translation t = translate(new IllegalStateException(
                "connect failed jdbc:postgresql://admin:hunter2@db:5432/people"));

        assertThat(t.body().getDetail())
                .doesNotContain("hunter2").doesNotContain("jdbc").doesNotContain("connect failed")
                .matches(".*errorId=[0-9a-f]{8}.*");
    }

    @Test
    void translate_Should_HideDetail_When_BusinessExceptionHasServerErrorCode() {
        Translation t = translate(new BusinessException(ErrorCode.WEAPON_SAVE_FAILED, "insert failed: SQL state 23505"));

        assertThat(t.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(t.body().getDetail()).doesNotContain("SQL").contains("errorId=");
    }

    @Test
    void translate_Should_MaskClientErrorDetail() {
        Translation t = translate(new IllegalArgumentException("bad refresh_token=rt-secret-1"));

        assertThat(t.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(t.body().getDetail()).contains("refresh_token=***").doesNotContain("rt-secret-1");
    }

    @Test
    void translate_Should_ExposeMaskedServerDetail_When_EnabledForDevelopment() {
        Translation t = ExceptionTranslator.withDefaults(true)
                .translate(new IllegalStateException("kaboom password=hunter2"), "/x");

        assertThat(t.body().getDetail()).contains("kaboom").contains("password=***").doesNotContain("hunter2");
    }

    @Test
    void translate_Should_LogMaskedMessageAndCauseChain_WithTheSameErrorId() {
        Translation t = translate(new IllegalStateException("outer token=tok-1",
                new RuntimeException("inner Bearer abc.def.ghi password=hunter2")));

        assertThat(logs.list).hasSize(1);
        ILoggingEvent event = logs.list.get(0);
        String errorId = t.body().getDetail().replaceAll(".*errorId=([0-9a-f]{8}).*", "$1");
        assertThat(event.getFormattedMessage()).contains(errorId).doesNotContain("tok-1");
        var thrown = event.getThrowableProxy();
        assertThat(thrown.getMessage()).contains("IllegalStateException").doesNotContain("tok-1");
        assertThat(thrown.getCause().getMessage()).doesNotContain("hunter2").doesNotContain("abc.def.ghi");
        assertThat(thrown.getStackTraceElementProxyArray()).isNotEmpty();
    }

    @Test
    void translate_Should_LogClientErrorsWithoutSecrets() {
        translate(new IllegalArgumentException("bad password=hunter2"));

        assertThat(logs.list).allSatisfy(e -> assertThat(e.getFormattedMessage()).doesNotContain("hunter2"));
    }

    @SuppressWarnings("unused")
    private void dummy(String arg) {
    }
}
