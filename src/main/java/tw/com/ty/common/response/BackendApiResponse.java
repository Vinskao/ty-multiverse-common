package tw.com.ty.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

/**
 * Backend API 响应类
 * 
 * <p>专门用于 Backend 业务服务的响应格式，包含业务特定的元数据：</p>
 * <ul>
 *   <li>请求 ID（用于异步操作追踪）</li>
 *   <li>总记录数（用于分页）</li>
 *   <li>当前页码和页大小</li>
 *   <li>错误详情</li>
 * </ul>
 * 
 * <p>响应示例：</p>
 * <pre>
 * {
 *   "success": true,
 *   "code": 200,
 *   "message": "查询成功",
 *   "timestamp": "2025-11-10T15:30:00Z",
 *   "requestId": "req-12345",
 *   "total": 100,
 *   "page": 1,
 *   "pageSize": 20,
 *   "data": [...]
 * }
 * </pre>
 * 
 * @param <T> 响应数据的类型
 * @author TY Backend Team
 * @version 1.0
 * @since 2025
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BackendApiResponse<T> extends BaseApiResponse<T> {

    /**
     * 请求 ID（用于异步操作追踪和日志关联）
     */
    @JsonProperty("requestId")
    private String requestId;

    /**
     * 总记录数（用于分页查询）
     */
    @JsonProperty("total")
    private Long total;

    /**
     * 当前页码（从 1 开始）
     */
    @JsonProperty("page")
    private Integer page;

    /**
     * 每页大小
     */
    @JsonProperty("pageSize")
    private Integer pageSize;

    /**
     * 错误详情（仅在失败时提供）
     */
    @JsonProperty("error")
    private String error;

    /**
     * 错误堆栈（仅在开发环境提供）
     */
    @JsonProperty("stackTrace")
    private String stackTrace;

    /**
     * 默认构造函数
     */
    public BackendApiResponse() {
        super();
    }

    /**
     * 完整构造函数
     * 
     * @param success 是否成功
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     */
    public BackendApiResponse(boolean success, int code, String message, T data) {
        super(success, code, message, data);
    }

    // ==================== 静态工厂方法 - 成功响应 ====================

    /**
     * 创建成功响应
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> success(T data) {
        return new BackendApiResponse<>(true, HttpStatus.OK.value(), "Success", data);
    }

    /**
     * 创建成功响应（带自定义消息）
     * 
     * @param message 自定义消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> success(String message, T data) {
        return new BackendApiResponse<>(true, HttpStatus.OK.value(), message, data);
    }

    /**
     * 创建成功响应（使用 MessageKey）
     * 
     * @param messageKey 消息键值
     * @param data 响应数据
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> success(MessageKey messageKey, T data) {
        return new BackendApiResponse<>(true, HttpStatus.OK.value(), messageKey.getMessage(), data);
    }

    /**
     * 创建成功响应（使用 MessageKey，无数据）
     * 
     * @param messageKey 消息键值
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> success(MessageKey messageKey) {
        return new BackendApiResponse<>(true, HttpStatus.OK.value(), messageKey.getMessage(), null);
    }

    /**
     * 创建成功响应（无数据）
     * 
     * @param message 成功消息
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> success(String message) {
        return new BackendApiResponse<>(true, HttpStatus.OK.value(), message, null);
    }

    /**
     * 创建分页成功响应
     * 
     * @param data 响应数据
     * @param total 总记录数
     * @param page 当前页码
     * @param pageSize 每页大小
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> successWithPagination(T data, long total, int page, int pageSize) {
        BackendApiResponse<T> response = new BackendApiResponse<>(
            true, 
            HttpStatus.OK.value(), 
            "Success", 
            data
        );
        response.setTotal(total);
        response.setPage(page);
        response.setPageSize(pageSize);
        return response;
    }

    /**
     * 创建异步请求已接受响应（202 Accepted）
     * 
     * @param requestId 请求 ID
     * @param message 提示消息
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> accepted(String requestId, String message) {
        BackendApiResponse<T> response = new BackendApiResponse<>(
            true, 
            HttpStatus.ACCEPTED.value(), 
            message, 
            null
        );
        response.setRequestId(requestId);
        return response;
    }

    /**
     * 创建异步请求已接受响应（使用 MessageKey）
     * 
     * @param requestId 请求 ID
     * @param messageKey 消息键值
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> accepted(String requestId, MessageKey messageKey) {
        BackendApiResponse<T> response = new BackendApiResponse<>(
            true, 
            HttpStatus.ACCEPTED.value(), 
            messageKey.getMessage(), 
            null
        );
        response.setRequestId(requestId);
        return response;
    }

    /**
     * 创建创建成功响应（201 Created）
     * 
     * @param data 创建的数据
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> created(T data) {
        return new BackendApiResponse<>(true, HttpStatus.CREATED.value(), "Created successfully", data);
    }

    // ==================== 静态工厂方法 - 失败响应 ====================

    /**
     * 创建失败响应
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> error(int code, String message) {
        return new BackendApiResponse<>(false, code, message, null);
    }

    /**
     * 创建失败响应（使用 ErrorCode）
     * 
     * @param errorCode 错误代码枚举
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> error(ErrorCode errorCode) {
        return new BackendApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 创建失败响应（使用 ErrorCode 带详细信息）
     * 
     * @param errorCode 错误代码枚举
     * @param detail 错误详细信息
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> error(ErrorCode errorCode, String detail) {
        BackendApiResponse<T> response = new BackendApiResponse<>(
            false, 
            errorCode.getCode(), 
            errorCode.getMessage(), 
            null
        );
        response.setError(detail);
        return response;
    }

    /**
     * 创建失败响应（使用 HttpStatus）
     * 
     * @param status HTTP 状态
     * @param message 错误消息
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> error(HttpStatus status, String message) {
        return new BackendApiResponse<>(false, status.value(), message, null);
    }

    /**
     * 创建失败响应（带错误详情）
     * 
     * @param status HTTP 状态
     * @param message 错误消息
     * @param error 错误详情
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> error(HttpStatus status, String message, String error) {
        BackendApiResponse<T> response = new BackendApiResponse<>(false, status.value(), message, null);
        response.setError(error);
        return response;
    }

    /**
     * 创建参数验证失败响应（400 Bad Request）
     * 
     * @param message 验证失败消息
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> badRequest(String message) {
        return new BackendApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), message, null);
    }

    /**
     * 创建未授权响应（401 Unauthorized）
     * 
     * @param message 未授权消息
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> unauthorized(String message) {
        return new BackendApiResponse<>(false, HttpStatus.UNAUTHORIZED.value(), message, null);
    }

    /**
     * 创建禁止访问响应（403 Forbidden）
     * 
     * @param message 禁止访问消息
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> forbidden(String message) {
        return new BackendApiResponse<>(false, HttpStatus.FORBIDDEN.value(), message, null);
    }

    /**
     * 创建资源未找到响应（404 Not Found）
     * 
     * @param message 未找到消息
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> notFound(String message) {
        return new BackendApiResponse<>(false, HttpStatus.NOT_FOUND.value(), message, null);
    }

    /**
     * 创建服务器内部错误响应（500 Internal Server Error）
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> internalError(String message) {
        return new BackendApiResponse<>(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), message, null);
    }

    /**
     * 创建服务器内部错误响应（带错误详情）
     * 
     * @param message 错误消息
     * @param error 错误详情
     * @param <T> 数据类型
     * @return Backend 响应对象
     */
    public static <T> BackendApiResponse<T> internalError(String message, String error) {
        BackendApiResponse<T> response = new BackendApiResponse<>(
            false, 
            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
            message, 
            null
        );
        response.setError(error);
        return response;
    }

    // ==================== Builder 模式 ====================

    /**
     * 设置请求 ID
     * 
     * @param requestId 请求 ID
     * @return 当前对象（支持链式调用）
     */
    public BackendApiResponse<T> withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    /**
     * 设置分页信息
     * 
     * @param total 总记录数
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 当前对象（支持链式调用）
     */
    public BackendApiResponse<T> withPagination(long total, int page, int pageSize) {
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        return this;
    }

    /**
     * 设置错误详情
     * 
     * @param error 错误详情
     * @return 当前对象（支持链式调用）
     */
    public BackendApiResponse<T> withError(String error) {
        this.error = error;
        return this;
    }

    /**
     * 设置堆栈追踪（仅开发环境）
     * 
     * @param stackTrace 堆栈追踪
     * @return 当前对象（支持链式调用）
     */
    public BackendApiResponse<T> withStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
        return this;
    }

    // ==================== Getters and Setters ====================

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
}

