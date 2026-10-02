package tw.com.ty.common.security.mask;

/**
 * Log-safe copy of an exception: same stack frames, but the message of every throwable in the cause chain is
 * passed through {@link SensitiveDataMasker} and prefixed with the original class name.
 */
public final class MaskedException extends RuntimeException {

    private static final int MAX_CAUSE_DEPTH = 10;

    private MaskedException(String message, Throwable cause) {
        super(message, cause, false, true);
    }

    public static MaskedException of(Throwable original) {
        return of(original, 0);
    }

    private static MaskedException of(Throwable original, int depth) {
        Throwable cause = original.getCause();
        MaskedException maskedCause = cause != null && cause != original && depth < MAX_CAUSE_DEPTH
                ? of(cause, depth + 1) : null;
        String message = original.getMessage() == null
                ? original.getClass().getName()
                : original.getClass().getName() + ": " + SensitiveDataMasker.mask(original.getMessage());
        MaskedException masked = new MaskedException(message, maskedCause);
        masked.setStackTrace(original.getStackTrace());
        return masked;
    }
}
