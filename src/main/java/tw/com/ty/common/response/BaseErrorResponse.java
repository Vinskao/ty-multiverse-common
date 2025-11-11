package tw.com.ty.common.response;

import java.time.LocalDateTime;

/**
 * 基礎錯誤響應類
 *
 * <p>為所有錯誤響應類提供通用的結構和行為：</p>
 * <ul>
 *   <li>錯誤代碼 (code)</li>
 *   <li>錯誤訊息 (message)</li>
 *   <li>詳細訊息 (detail)</li>
 *   <li>時間戳 (timestamp)</li>
 *   <li>請求路徑 (path)</li>
 * </ul>
 *
 * @author TY Backend Team
 * @version 1.0
 * @since 2025
 */
public abstract class BaseErrorResponse {

    protected final int code;
    protected final String message;
    protected final String detail;
    protected final LocalDateTime timestamp;
    protected final String path;

    protected BaseErrorResponse(int code, String message, String detail, LocalDateTime timestamp, String path) {
        this.code = code;
        this.message = message;
        this.detail = detail;
        this.timestamp = timestamp;
        this.path = path;
    }

    /**
     * 獲取錯誤代碼
     */
    public int getCode() {
        return code;
    }

    /**
     * 獲取錯誤訊息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 獲取詳細訊息
     */
    public String getDetail() {
        return detail;
    }

    /**
     * 獲取時間戳
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * 獲取請求路徑
     */
    public String getPath() {
        return path;
    }
}
