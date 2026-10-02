package tw.com.ty.common.exception.handler.impl;

/**
 * Class-name based instanceof, so handlers for optional libraries (spring-security, spring-orm, ...)
 * can be loaded in applications that do not have those libraries on the classpath.
 */
final class ThrowableTypes {

    private ThrowableTypes() {
    }

    static boolean isInstanceOf(Throwable ex, String... classNames) {
        for (Class<?> c = ex.getClass(); c != null; c = c.getSuperclass()) {
            for (String name : classNames) {
                if (c.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
}
