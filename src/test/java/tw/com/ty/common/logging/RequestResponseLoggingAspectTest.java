package tw.com.ty.common.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class RequestResponseLoggingAspectTest {

    private final RequestResponseLoggingAspect aspect = new RequestResponseLoggingAspect();
    private ListAppender<ILoggingEvent> logs;

    @BeforeEach
    void captureLogs() {
        logs = new ListAppender<>();
        logs.start();
        ((Logger) LoggerFactory.getLogger(RequestResponseLoggingAspect.class)).addAppender(logs);
    }

    @AfterEach
    void releaseLogs() {
        ((Logger) LoggerFactory.getLogger(RequestResponseLoggingAspect.class)).detachAppender(logs);
    }

    private static ProceedingJoinPoint call(String[] names, Object... args) {
        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        MethodSignature sig = mock(MethodSignature.class);
        when(jp.getArgs()).thenReturn(args);
        when(jp.getSignature()).thenReturn(sig);
        when(sig.getParameterNames()).thenReturn(names);
        return jp;
    }

    @Test
    void requestParameters_Should_MaskOpaqueTokensByParameterName() {
        String described = aspect.getRequestParameters(call(new String[] { "refreshToken", "name" }, "opaque-rt-1", "Bob"));

        assertThat(described).contains("refreshToken=***").contains("name=Bob").doesNotContain("opaque-rt-1");
    }

    @Test
    void requestParameters_Should_MaskSecretsInsideObjects() {
        Object body = new Object() {
            @Override
            public String toString() {
                return "Login{user=bob, password=hunter2}";
            }
        };
        assertThat(aspect.getRequestParameters(call(new String[] { "login" }, body))).doesNotContain("hunter2");
    }

    @Test
    void requestParameters_Should_StillWork_When_ParameterNamesUnavailable() {
        assertThat(aspect.getRequestParameters(call(null, "Bearer abc.def.ghi"))).doesNotContain("abc.def.ghi");
    }

    @Test
    void responseContent_Should_BeMasked() {
        String described = aspect.truncateResponse(Map.of("access_token", "at-secret-1", "user", "bob"), 200);

        assertThat(described).doesNotContain("at-secret-1").contains("bob");
    }

    @Test
    void apiResponseDetails_Should_LogMaskedErrorField() {
        class ApiResponse {
            @SuppressWarnings("unused") private final boolean success = false;
            @SuppressWarnings("unused") private final int code = 500;
            @SuppressWarnings("unused") private final String message = "failed for bob@example.com";
            @SuppressWarnings("unused") private final String error = "jdbc:postgresql://admin:hunter2@db/people";
        }
        aspect.logApiResponseDetails(new ApiResponse(), "req1", 500);

        assertThat(logs.list).isNotEmpty().allSatisfy(e -> assertThat(e.getFormattedMessage())
                .doesNotContain("hunter2").doesNotContain("bob@example.com"));
    }
}
