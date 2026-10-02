package tw.com.ty.common.logging.mask;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import tw.com.ty.common.security.mask.SensitiveDataMasker;

/** Logback {@code %m}/{@code %msg} replacement that masks credentials and personal data in every log message. */
public class MaskingMessageConverter extends MessageConverter {

    @Override
    public String convert(ILoggingEvent event) {
        return SensitiveDataMasker.mask(super.convert(event));
    }
}
