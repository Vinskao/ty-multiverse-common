package tw.com.ty.common.transaction.annotation;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tw.com.ty.common.exception.BusinessException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TY Multiverse 統一事務註解
 *
 * <p>統一的事務配置，適用於所有業務操作：</p>
 * <ul>
 *   <li>讀取操作：使用 {@link #READ_ONLY}</li>
 *   <li>寫入操作：使用 {@link #REQUIRED}</li>
 *   <li>特殊場景：根據具體需求選擇</li>
 * </ul>
 *
 * <p>異常處理策略：</p>
 * <ul>
 *   <li>{@link RuntimeException} 和其子類：自動回滾</li>
 *   <li>檢查型異常：不會自動回滾</li>
 *   <li>業務異常：根據具體需求配置</li>
 * </ul>
 *
 * @author TY Backend Team
 * @version 1.0
 * @since 2025
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Transactional
public @interface TyTransactional {

    /**
     * 事務傳播行為
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * 事務隔離級別
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * 超時時間（秒），-1表示使用默認
     */
    int timeout() default -1;

    /**
     * 是否為只讀事務
     */
    boolean readOnly() default false;

    /**
     * 不會觸發回滾的異常類型
     */
    Class<? extends Throwable>[] noRollbackFor() default {};

    /**
     * 會觸發回滾的異常類型
     */
    Class<? extends Throwable>[] rollbackFor() default {};

    /**
     * 預定義的常用事務配置
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional(readOnly = true)
    @interface READ_ONLY {
    }

    /**
     * 寫入操作的標準事務配置
     * - 會為 RuntimeException 自動回滾
     * - 不會為檢查型異常回滾
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional(
        propagation = org.springframework.transaction.annotation.Propagation.REQUIRED,
        isolation = org.springframework.transaction.annotation.Isolation.DEFAULT,
        rollbackFor = {RuntimeException.class}
    )
    @interface REQUIRED {
    }

    /**
     * 新建事務的配置
     * - 總是開啟新的事務
     * - 與當前事務完全隔離
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional(
        propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW,
        rollbackFor = {RuntimeException.class}
    )
    @interface REQUIRES_NEW {
    }

    /**
     * 業務操作的標準配置
     * - 適用於大部分業務寫入操作
     * - 包含常見的業務異常處理
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional(
        propagation = org.springframework.transaction.annotation.Propagation.REQUIRED,
        rollbackFor = {RuntimeException.class, BusinessException.class}
    )
    @interface BUSINESS {
    }

    /**
     * 批次操作的事務配置
     * - 較長的超時時間
     * - 支持大批量數據處理
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional(
        propagation = org.springframework.transaction.annotation.Propagation.REQUIRED,
        timeout = 300, // 5分鐘
        rollbackFor = {RuntimeException.class}
    )
    @interface BATCH {
    }
}
