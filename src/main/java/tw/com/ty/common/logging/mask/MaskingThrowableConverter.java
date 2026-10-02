package tw.com.ty.common.logging.mask;

import ch.qos.logback.classic.pattern.ExtendedThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.CoreConstants;
import tw.com.ty.common.security.mask.SensitiveDataMasker;

/**
 * Logback stack-trace converter ({@code %maskedEx}) that masks the message of every throwable in the cause chain
 * while keeping the frames. Like Spring Boot's {@code %wEx}, it starts the stack trace on its own line.
 */
public class MaskingThrowableConverter extends ExtendedThrowableProxyConverter {

    @Override
    protected String throwableProxyToString(IThrowableProxy proxy) {
        return CoreConstants.LINE_SEPARATOR + SensitiveDataMasker.mask(super.throwableProxyToString(proxy));
    }
}
