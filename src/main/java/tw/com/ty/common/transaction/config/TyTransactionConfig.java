package tw.com.ty.common.transaction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * TY Multiverse 統一事務配置
 *
 * <p>啟用並配置全局事務管理：</p>
 * <ul>
 *   <li>啟用 @Transactional 註解</li>
 *   <li>配置預設事務管理器</li>
 *   <li>定義事務回滾規則</li>
 * </ul>
 *
 * <p>事務回滾規則：</p>
 * <ul>
 *   <li>{@link RuntimeException} 及其子類：自動回滾</li>
 *   <li>檢查型異常（checked exceptions）：不會自動回滾</li>
 *   <li>業務異常：根據具體配置決定</li>
 * </ul>
 *
 * @author TY Backend Team
 * @version 1.0
 * @since 2025
 */
@Configuration
@EnableTransactionManagement
public class TyTransactionConfig implements TransactionManagementConfigurer {

    private final PlatformTransactionManager transactionManager;

    public TyTransactionConfig(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * 配置預設事務管理器
     * Backend 應使用 JpaTransactionManager
     * Gateway 應使用 ReactiveTransactionManager（如果需要）
     */
    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return transactionManager;
    }
}
