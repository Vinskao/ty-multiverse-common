package tw.com.ty.common.resilience;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import tw.com.ty.common.resilience.annotation.Retryable;

import java.util.HashMap;
import java.util.Map;

/**
 * 重試切面
 *
 * 處理 @Retryable 註解的方法重試邏輯
 */
@Aspect
@Component
public class RetryAspect {

    private static final Logger logger = LoggerFactory.getLogger(RetryAspect.class);

    @Autowired
    private RetryTemplate defaultRetryTemplate;

    @Around("@annotation(retryable)")
    public Object retryOnException(ProceedingJoinPoint joinPoint, Retryable retryable) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        logger.debug("🔄 Applying retry logic to method: {}", methodName);

        // 創建自定義的重試模板
        RetryTemplate retryTemplate = createRetryTemplate(retryable);

        try {
            return retryTemplate.execute(context -> {
                int attempt = context.getRetryCount() + 1;
                logger.debug("🎯 Executing method {} (attempt {})", methodName, attempt);

                try {
                    Object result = joinPoint.proceed();
                    logger.debug("✅ Method {} succeeded on attempt {}", methodName, attempt);
                    return result;
                } catch (Throwable e) {
                    logger.warn("❌ Method {} failed on attempt {}: {}", methodName, attempt, e.getMessage());

                    // 檢查是否是可重試的異常
                    if (isRetryableException(e, retryable)) {
                        logger.info("🔄 Retrying method {} due to: {}", methodName, e.getClass().getSimpleName());
                        throw e; // 重新拋出異常以觸發重試
                    } else {
                        logger.warn("🚫 Not retrying method {} for non-retryable exception: {}", methodName, e.getClass().getSimpleName());
                        throw new RuntimeException(e); // 包裝為 RuntimeException 避免重試
                    }
                }
            });
        } catch (Exception e) {
            logger.error("💥 Method {} failed after all retry attempts: {}", methodName, e.getMessage());
            throw e.getCause() != null ? e.getCause() : e;
        }
    }

    private RetryTemplate createRetryTemplate(Retryable retryable) {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 配置退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retryable.initialDelay());
        backOffPolicy.setMultiplier(retryable.multiplier());
        backOffPolicy.setMaxInterval(retryable.maxDelay());
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 配置重試策略
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        for (Class<? extends Throwable> exceptionClass : retryable.value()) {
            retryableExceptions.put(exceptionClass, true);
        }

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(retryable.maxAttempts(), retryableExceptions, true);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    private boolean isRetryableException(Throwable e, Retryable retryable) {
        for (Class<? extends Throwable> exceptionClass : retryable.value()) {
            if (exceptionClass.isAssignableFrom(e.getClass())) {
                return true;
            }
        }
        return false;
    }
}
