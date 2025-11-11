package tw.com.ty.common.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 重試配置類
 *
 * 提供統一的重試機制配置
 */
@Configuration
@EnableRetry
public class RetryConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RetryConfiguration.class);

    /**
     * 通用重試模板
     */
    @Bean
    public RetryTemplate defaultRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 指數退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // 1秒
        backOffPolicy.setMultiplier(2.0); // 每次重試間隔翻倍
        backOffPolicy.setMaxInterval(30000); // 最大間隔30秒
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 重試策略 - 通用異常
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(Exception.class, true); // 所有異常都重試

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions, true);
        retryTemplate.setRetryPolicy(retryPolicy);

        logger.info("✅ Default RetryTemplate configured: maxAttempts={}, backOff=exponential", 3);
        return retryTemplate;
    }

    /**
     * 數據庫連接重試模板
     */
    @Bean
    public RetryTemplate databaseRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 指數退避策略 - 更長的等待時間
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(5000); // 5秒
        backOffPolicy.setMultiplier(2.0); // 每次重試間隔翻倍
        backOffPolicy.setMaxInterval(60000); // 最大間隔60秒
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 重試策略 - 數據庫連接相關異常
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(org.springframework.transaction.CannotCreateTransactionException.class, true);
        retryableExceptions.put(java.sql.SQLTransientConnectionException.class, true);
        retryableExceptions.put(org.springframework.dao.DataAccessException.class, true);
        // R2DBC 異常會動態添加（如果存在）

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(10, retryableExceptions, true); // 最多10次重試
        retryTemplate.setRetryPolicy(retryPolicy);

        logger.info("✅ Database RetryTemplate configured: maxAttempts={}, backOff=exponential", 10);
        return retryTemplate;
    }

    /**
     * 網路調用重試模板
     */
    @Bean
    public RetryTemplate networkRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 指數退避策略 - 網路調用適中的等待時間
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(2000); // 2秒
        backOffPolicy.setMultiplier(1.5); // 適中的倍數
        backOffPolicy.setMaxInterval(15000); // 最大間隔15秒
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 重試策略 - 網路相關異常
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(java.net.ConnectException.class, true);
        retryableExceptions.put(java.net.SocketTimeoutException.class, true);
        retryableExceptions.put(java.io.IOException.class, true);
        retryableExceptions.put(org.springframework.web.client.ResourceAccessException.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(5, retryableExceptions, true); // 最多5次重試
        retryTemplate.setRetryPolicy(retryPolicy);

        logger.info("✅ Network RetryTemplate configured: maxAttempts={}, backOff=exponential", 5);
        return retryTemplate;
    }
}
