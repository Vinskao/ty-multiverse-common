package tw.com.ty.common.resilience.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 重試註解
 *
 * 標記方法在遇到特定異常時應自動重試
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retryable {

    /**
     * 要重試的異常類型
     */
    Class<? extends Throwable>[] value() default {Exception.class};

    /**
     * 最大重試次數
     */
    int maxAttempts() default 3;

    /**
     * 初始延遲時間（毫秒）
     */
    long initialDelay() default 1000;

    /**
     * 最大延遲時間（毫秒）
     */
    long maxDelay() default 30000;

    /**
     * 延遲倍數
     */
    double multiplier() default 2.0;

    /**
     * 異常處理器類
     */
    Class<?>[] exceptionHandlers() default {};
}
