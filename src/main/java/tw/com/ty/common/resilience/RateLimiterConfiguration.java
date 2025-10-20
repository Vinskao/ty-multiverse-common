package tw.com.ty.common.resilience;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 通用 Rate Limiter 配置類別
 *
 * 提供可配置的 Rate Limiter 保護，防止 DDOS 攻擊
 */
@Configuration
public class RateLimiterConfiguration {

    @Value("${rate-limiter.api.capacity:100}")
    private int apiCapacity;

    @Value("${rate-limiter.api.refill-tokens:100}")
    private int apiRefillTokens;

    @Value("${rate-limiter.api.refill-duration-seconds:1}")
    private int apiRefillDurationSeconds;

    @Value("${rate-limiter.batch.capacity:50}")
    private int batchCapacity;

    @Value("${rate-limiter.batch.refill-tokens:50}")
    private int batchRefillTokens;

    @Value("${rate-limiter.batch.refill-duration-seconds:1}")
    private int batchRefillDurationSeconds;

    /**
     * 創建通用API的Rate Limiter
     *
     * @return Bucket 實例
     */
    @Bean("commonApiRateLimiter")
    public Bucket apiRateLimiter() {
        Bandwidth limit = Bandwidth.classic(apiCapacity, Refill.greedy(apiRefillTokens, Duration.ofSeconds(apiRefillDurationSeconds)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * 創建批量API的Rate Limiter
     *
     * @return Bucket 實例
     */
    @Bean("commonBatchApiRateLimiter")
    public Bucket batchApiRateLimiter() {
        Bandwidth limit = Bandwidth.classic(batchCapacity, Refill.greedy(batchRefillTokens, Duration.ofSeconds(batchRefillDurationSeconds)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
