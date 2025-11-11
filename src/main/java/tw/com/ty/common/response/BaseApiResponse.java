package tw.com.ty.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * 基础 API 响应抽象类
 * 
 * <p>定义所有 API 响应的通用结构，包括状态码、消息、时间戳和数据。
 * 这个抽象类为 Gateway 和 Backend 提供统一的响应格式基础。</p>
 * 
 * <p>响应结构：</p>
 * <pre>
 * {
 *   "success": true/false,
 *   "code": 200,
 *   "message": "操作成功",
 *   "timestamp": "2025-11-10T15:30:00Z",
 *   "data": {...}
 * }
 * </pre>
 * 
 * @param <T> 响应数据的类型
 * @author TY Backend Team
 * @version 1.0
 * @since 2025
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseApiResponse<T> {

    /**
     * 操作是否成功
     */
    @JsonProperty("success")
    private boolean success;

    /**
     * 响应状态码（HTTP 状态码或自定义业务码）
     */
    @JsonProperty("code")
    private int code;

    /**
     * 响应消息
     */
    @JsonProperty("message")
    private String message;

    /**
     * 响应时间戳（ISO-8601 格式）
     */
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String timestamp;

    /**
     * 响应数据
     */
    @JsonProperty("data")
    private T data;

    /**
     * 默认构造函数
     */
    protected BaseApiResponse() {
        this.timestamp = Instant.now().toString();
    }

    /**
     * 完整构造函数
     * 
     * @param success 是否成功
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     */
    protected BaseApiResponse(boolean success, int code, String message, T data) {
        this();
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ==================== Getters and Setters ====================

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return String.format("%s{success=%s, code=%d, message='%s', timestamp='%s', data=%s}",
                this.getClass().getSimpleName(), success, code, message, timestamp, data);
    }
}

