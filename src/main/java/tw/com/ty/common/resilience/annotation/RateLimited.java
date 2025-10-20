package tw.com.ty.common.resilience.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rate Limiter 註解
 *
 * 用於標註需要進行 Rate Limiting 的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    /**
     * Rate Limiter 類型
     */
    RateLimitType value() default RateLimitType.API;

    /**
     * 自訂描述
     */
    String description() default "";
}
