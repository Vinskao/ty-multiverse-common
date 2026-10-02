package tw.com.ty.common.logging.mask;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * Last line of defence: whatever any code logs (its own message, exception messages, cause chains),
 * the layout masks it before it is written.
 */
class MaskingConvertersTest {

    private final LoggerContext context = new LoggerContext();
    private final Logger logger = context.getLogger("test");

    private String render(String pattern, String message, Throwable error) {
        PatternLayout layout = new PatternLayout();
        layout.setContext(context);
        layout.getInstanceConverterMap().put("maskedMsg", MaskingMessageConverter.class.getName());
        layout.getInstanceConverterMap().put("maskedEx", MaskingThrowableConverter.class.getName());
        layout.setPattern(pattern);
        layout.start();
        return layout.doLayout(new LoggingEvent("fqcn", logger, Level.ERROR, message, error, null));
    }

    @Test
    void message_Should_BeMasked() {
        String line = render("%maskedMsg", "login failed for bob@example.com with password=hunter2", null);

        assertThat(line).doesNotContain("hunter2").doesNotContain("bob@example.com").contains("password=***");
    }

    @Test
    void throwable_Should_MaskEveryMessageInTheCauseChain_ButKeepFrames() {
        Exception error = new IllegalStateException("query failed jdbc:postgresql://admin:hunter2@db/people",
                new RuntimeException("Bearer abc.def.ghi rejected"));

        String out = render("%maskedMsg%n%maskedEx", "boom", error);

        assertThat(out).doesNotContain("hunter2").doesNotContain("abc.def.ghi")
                .contains("IllegalStateException").contains("Caused by").contains("at ");
    }

    @Test
    void throwable_Should_RenderNothing_When_NoException() {
        assertThat(render("%maskedEx", "plain", null)).isEmpty();
    }

    @Test
    void includeFile_Should_RegisterMaskingForSpringBootDefaultPatterns() throws Exception {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("tw/com/ty/common/logging/logback-masking.xml")) {
            assertThat(in).isNotNull();
            String xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(xml).contains("conversionWord=\"m\"").contains("conversionWord=\"msg\"")
                    .contains("conversionWord=\"maskedEx\"").contains(MaskingMessageConverter.class.getName())
                    // Spring Boot's default patterns end with ${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}
                    .contains("name=\"LOG_EXCEPTION_CONVERSION_WORD\" value=\"%maskedEx\"");
        }
    }
}
