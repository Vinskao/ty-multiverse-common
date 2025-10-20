package tw.com.ty.common.resilience;

import io.github.bucket4j.Bucket;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import tw.com.ty.common.exception.ResilienceException;

/**
 * 通用 Rate Limiter AOP切面
 *
 * 提供可配置的 Rate Limiter 保護，防止 DDOS 攻擊
 * 支援通過配置指定要攔截的套件和方法
 */
@Aspect
@Component
public class RateLimiterAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterAspect.class);

    private final Bucket apiRateLimiter;
    private final Bucket batchApiRateLimiter;

    @Autowired
    public RateLimiterAspect(@Qualifier("commonApiRateLimiter") Bucket apiRateLimiter,
                            @Qualifier("commonBatchApiRateLimiter") Bucket batchApiRateLimiter) {
        this.apiRateLimiter = apiRateLimiter;
        this.batchApiRateLimiter = batchApiRateLimiter;
    }

    /**
     * 應用 Rate Limiter 保護
     */
    private Object applyRateLimit(ProceedingJoinPoint joinPoint, Bucket bucket, String apiType) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringTypeName();

        // Rate Limiter 檢查
        if (!bucket.tryConsume(1)) {
            logger.warn("{} - {}: Rate Limiter 限制，請稍後再試", className, methodName);
            throw ResilienceException.rateLimitExceeded();
        }

        // 通過 Rate Limiter，執行實際業務邏輯
        logger.debug("{} - {}: 請求通過 Rate Limiter", className, methodName);
        return joinPoint.proceed();
    }

    /**
     * 攔截通用API請求 - 預設規則
     * 攔截所有標註 @RateLimited 的方法
     */
    @Around("@annotation(rateLimited)")
    public Object rateLimitAnnotatedMethods(ProceedingJoinPoint joinPoint,
                                          tw.com.ty.common.resilience.annotation.RateLimited rateLimited) throws Throwable {
        Bucket bucket = rateLimited.value() == tw.com.ty.common.resilience.annotation.RateLimitType.BATCH
                       ? batchApiRateLimiter : apiRateLimiter;
        return applyRateLimit(joinPoint, bucket, "RateLimited API");
    }

    /**
     * 攔截通用API請求 - 通過點表達式配置
     * 此方法預設禁用，需要在具體專案中啟用並配置相應的點表達式
     */
    // @Around("execution(* com.example.controller.*.*(..)) && !execution(* *..batch*(..))")
    // public Object rateLimitConfiguredApis(ProceedingJoinPoint joinPoint) throws Throwable {
    //     return applyRateLimit(joinPoint, apiRateLimiter, "Configured API");
    // }

    /**
     * 攔截批量處理相關的API請求 - 通過點表達式配置
     * 此方法預設禁用，需要在具體專案中啟用並配置相應的點表達式
     */
    // @Around("execution(* com.example.controller.*.batch*(..))")
    // public Object rateLimitBatchApis(ProceedingJoinPoint joinPoint) throws Throwable {
    //     return applyRateLimit(joinPoint, batchApiRateLimiter, "Batch API");
    // }
}
